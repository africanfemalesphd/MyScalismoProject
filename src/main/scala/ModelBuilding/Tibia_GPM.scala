package ModelBuilding

import breeze.linalg.DenseVector
import scalismo.common.interpolation.{BarycentricInterpolator3D, TriangleMeshInterpolator3D}
import scalismo.common.{EuclideanSpace3D, Field}
import scalismo.geometry.{EuclideanVector, Point, _3D}
import scalismo.io.MeshIO
import scalismo.kernels.{DiagonalKernel, DiagonalKernel3D, GaussianKernel, GaussianKernel3D}
import scalismo.mesh.TriangleMesh
import scalismo.numerics.{FixedPointsUniformMeshSampler3D, LBFGSOptimizer}
import scalismo.registration.{GaussianProcessTransformationSpace, L2Regularizer, MeanSquaresMetric, Registration}
import scalismo.statisticalmodel.{GaussianProcess, LowRankGaussianProcess, PointDistributionModel}
import scalismo.ui.api.ScalismoUI

import java.io.File

object Tibia_GPM {
  def main(args: Array[String]) = {
    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)

    //val referenceMesh = MeshIO.readMesh(new java.io.File("datasets/quickstart/facemesh.ply")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/CT2_Talus_Remeshed_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/CT2_Calcaneus_Remeshed_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/CT2_Tibia_Remeshed_ref.stl")).get
    val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/CT2_Fibula_Remeshed_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Cuboid_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Navicular_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Intermedial_Cuneiform_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Lateral_Cuneiform_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Medial_Cuneiform_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/1_Metatarsal_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/2_Metatarsal_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/3_Metatarsal_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/4_Metatarsal_ref.stl")).get
    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/5_Metatarsal_ref.stl")).get



    val modelGroup = ui.createGroup("model")
    val refMeshView = ui.show(modelGroup, referenceMesh, "referenceMesh")
    refMeshView.color = java.awt.Color.RED
    /*
    val targetGroup = ui.createGroup("target")
    val targetMesh = MeshIO.readMesh(new java.io.File("data/Objects/Left/Hindfoot/Talus/initial/triangle-meshes/CT1_Talus.stl")).get
    val targetMeshView = ui.show(targetGroup, targetMesh, "targetMesh")
    val zeroMean = Field(EuclideanSpace3D, (_: Point[_3D]) => EuclideanVector.zeros[_3D])
    val kernel = DiagonalKernel3D(GaussianKernel3D(sigma = 70) * 50.0, outputDim = 3)
    val gp = GaussianProcess(zeroMean, kernel)
/*
    val k = DiagonalKernel(GaussianKernel[_3D](40) * 10, 3) +
      DiagonalKernel(GaussianKernel[_3D](20) * 5, 3) +
      DiagonalKernel(GaussianKernel[_3D](10) * 1, 3)

    val gp = GaussianProcess[_3D, EuclideanVector[_3D]](k) */

    val lowRankGP = LowRankGaussianProcess.approximateGPCholesky(
      referenceMesh,
      gp,
      1e-2,
      interpolator = TriangleMeshInterpolator3D[EuclideanVector[_3D]])

    val gpModel = PointDistributionModel(referenceMesh, lowRankGP)
    val GPModel = ui.show(modelGroup,gpModel, " model" ) */


    //Gaussian kernel
    val zeroMean = Field(EuclideanSpace3D, (_: Point[_3D]) => EuclideanVector.zeros[_3D])
    //val kernel = DiagonalKernel3D(GaussianKernel3D(sigma = 70) * 50.0, outputDim = 3) //sigma = 50 works better
//    val kernel = DiagonalKernel(GaussianKernel[_3D](40) * 10, 3) +
//      DiagonalKernel(GaussianKernel[_3D](20) * 5, 3) +
//      DiagonalKernel(GaussianKernel[_3D](10) * 1, 3)
    //for the fibula only
    val kernel = DiagonalKernel(GaussianKernel[_3D](40) * 10, 3) +
      DiagonalKernel(GaussianKernel[_3D](5) * 5, 3) +
      DiagonalKernel(GaussianKernel[_3D](10) * 1, 3)
    //Fibula only especially CT1
//    val kernel = DiagonalKernel(GaussianKernel[_3D](40) * 10, 3) +
//      DiagonalKernel(GaussianKernel[_3D](5) * 5, 3) +
//      DiagonalKernel(GaussianKernel[_3D](10) * 1, 3)
    val gp = GaussianProcess(zeroMean, kernel)
    //Low-rand approximation
    val interpolator = TriangleMeshInterpolator3D[EuclideanVector[_3D]]()
    val lowRankGP = LowRankGaussianProcess.approximateGPCholesky(
      referenceMesh,
      gp,
      relativeTolerance = 0.05,
      interpolator = interpolator)
    //Visualise the effect of the Gaussina kernel
    val gpView = ui.addTransformation(modelGroup, lowRankGP, "gp")


    //Registration of target mesh
    val targetGroup = ui.createGroup("target")
    val AlignMeshes = (1 until 6).map { i: Int =>
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes/CT" + i + "_Aligned_Talus.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Calcaneus/aligned/triangle-meshes/CT" + i + "_Aligned_Calcaneus.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes/CT" + i + "_Aligned_Tibia.stl")).get
      val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Fibula/aligned/triangle-meshes/CT" + i + "_Aligned_Fibula.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Midfoot/Cuboid/aligned/triangle-meshes/CT" + i + "_Aligned_Cuboid.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Midfoot/Navicular/aligned/triangle-meshes/CT" + i + "_Aligned_Navicular.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Midfoot/Intermedial_Cuneiform/aligned/triangle-meshes/CT" + i + "_Aligned_Intermedial_Cuneiform.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Midfoot/Lateral_Cuneiform/aligned/triangle-meshes/CT" + i + "_Aligned_Lateral_Cuneiform.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Midfoot/Med_Cuneiform/aligned/triangle-meshes/CT" + i + "_Aligned_Medial_Cuneiform.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/1_Metatarsal/aligned/triangle-meshes/CT" + i + "_Aligned_1_Metatarsal.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/2_Metatarsal/aligned/triangle-meshes/CT" + i + "_Aligned_2_Metatarsal.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/3_Metatarsal/aligned/triangle-meshes/CT" + i + "_Aligned_3_Metatarsal.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/4_Metatarsal/aligned/triangle-meshes/CT" + i + "_Aligned_4_Metatarsal.stl")).get
      //val targetMesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/5_Metatarsal/aligned/triangle-meshes/CT" + i + "_Aligned_5_Metatarsal.stl")).get



      //ui.show(targetGroup2, mesh, "Unaligned_FemurMeshes" + i)

     // val targetMesh = MeshIO.readMesh(new java.io.File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes/CT3_Aligned_Talus.stl")).get
      //val targetMeshView = ui.show(targetGroup, targetMesh, "targetMesh")
      //use the Gaussian process that we have defined above to define the transformation space.
      val transformationSpace = GaussianProcessTransformationSpace(lowRankGP)
      //The sampler determines the points where the metric is evaluated. In our case we choose uniformely sampled points on the reference mesh
      val fixedImage = referenceMesh.operations.toDistanceImage
      val movingImage = targetMesh.operations.toDistanceImage


      val sampler = FixedPointsUniformMeshSampler3D(referenceMesh, numberOfPoints = 2000)
      val metric = MeanSquaresMetric(fixedImage, movingImage, transformationSpace, sampler)
      //optimizer
      val optimizer = LBFGSOptimizer(maxNumberOfIterations = 1000)
      //Regularizer
      val regularizer = L2Regularizer(transformationSpace)
      //
      //Define Scalimo's registration Object
      val registration = Registration(metric, regularizer, regularizationWeight = 1e-5, optimizer)
      //Define start position for the iterator
      val initialCoefficients = DenseVector.zeros[Double](lowRankGP.rank)
      val registrationIterator = registration.iterator(initialCoefficients)
      //Visualise iteration
      val visualizingRegistrationIterator = for ((it, itnum) <- registrationIterator.zipWithIndex) yield {
        println(s"object value in iteration $itnum is ${it.value}")
        gpView.coefficients = it.parameters
        it
      }
      //We are interested in the last value
      val registrationResult = visualizingRegistrationIterator.toSeq.last
      //Obtaining final mesh
      val registrationTransformation = transformationSpace.transformationForParameters(registrationResult.parameters)
      val fittedMesh = referenceMesh.transform(registrationTransformation)

      //Finding the exact projection
      val targetMeshOperations = targetMesh.operations
      val projection = (pt: Point[_3D]) => {
        targetMeshOperations.closestPointOnSurface(pt).point
      }
      val finalTransformation = registrationTransformation.andThen(projection)
      val projectedMesh = referenceMesh.transform(finalTransformation)
      val resultGroup = ui.createGroup("result")
      val projectionView = ui.show(resultGroup, projectedMesh, "projection" +i)
      val fittedMeshView = ui.show(resultGroup, fittedMesh, "StartingPoint")
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes-Remeshed/CT"+i+"_Talus.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Hindfoot/Calcaneus/aligned/triangle-meshes-Remeshed/CT"+i+"_Calcaneus.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes-Remeshed/CT"+i+"_Tibia.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Shin_Bones/Fibula/aligned/triangle-meshes-Remeshed/CT"+i+"_Fibula.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Midfoot/Cuboid/aligned/triangle-meshes-Remeshed/CT" + i + "_Cuboid.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Midfoot/Navicular/aligned/triangle-meshes-Remeshed/CT" + i + "_Navicular.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Midfoot/Intermedial_Cuneiform/aligned/triangle-meshes-Remeshed/CT" + i + "_Intermedial_Cuneiform.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Midfoot/Lateral_Cuneiform/aligned/triangle-meshes-Remeshed/CT" + i + "_Lateral_Cuneiform.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Midfoot/Med_Cuneiform/aligned/triangle-meshes-Remeshed/CT" + i + "_Medial_Cuneiform.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Forefoot/1_Metatarsal/aligned/triangle-meshes-Remeshed/CT" + i + "_1_Metatarsal.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Forefoot/2_Metatarsal/aligned/triangle-meshes-Remeshed/CT" + i + "_2_Metatarsal.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Forefoot/3_Metatarsal/aligned/triangle-meshes-Remeshed/CT" + i + "_3_Metatarsal.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Forefoot/4_Metatarsal/aligned/triangle-meshes-Remeshed/CT" + i + "_4_Metatarsal.stl"))
      //MeshIO.writeMesh(projectedMesh, new File("data/Objects/Left/Forefoot/5_Metatarsal/aligned/triangle-meshes-Remeshed/CT" + i + "_5_Metatarsal.stl"))





      println("Oui Fini")


    }

  }
}
