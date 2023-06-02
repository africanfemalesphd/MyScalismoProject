package ModelBuilding

import breeze.linalg.Axis._1
import breeze.storage.ConfigurableDefault.fromV
import scalismo.common.interpolation.TriangleMeshInterpolator3D
import scalismo.common.{DiscreteField3D, PointId}
import scalismo.geometry._3D
import scalismo.io.{LandmarkIO, MeshIO}
import scalismo.mesh.TriangleMesh
import scalismo.numerics.PivotedCholesky.RelativeTolerance
import scalismo.statisticalmodel.{DiscreteLowRankGaussianProcess, PointDistributionModel}
import scalismo.statisticalmodel.dataset.DataCollection
import scalismo.ui.api.ScalismoUI

import java.io.File
import scala.collection.immutable

object Model_From_Data {

  def main(args: Array[String]):Unit = {
    // required to initialize native libraries (VTK, HDF5 ..)
    scalismo.initialize()
    // setting a seed for the random generator to allow for reproducible results
    implicit val rng = scalismo.utils.Random(42)

    val ui = ScalismoUI()

    val targetGroup = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")
    val targetGroup3 = ui.createGroup("target3")

    //val reference = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes/CT2_Aligned_Talus.stl")).get
    //    val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Hindfoot/Talus/initial/landmarks/CT1_Talus.json")).get
    //ui.show(targetGroup, reference, "referenceMesh")
    //    //ui.show(targetGroup, refLmks, "referenceLandmarks")
    //
    //
    //
 /*   val Alignedmeshes = (1 until 11).map { i =>
      val meshes: TriangleMesh[_3D] = MeshIO.readMesh(new File(s"data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes/CT${i}_Aligned_Talus.stl")).get
      //ui.show(targetGroup2, mesh, "Unaligned_FemurMeshes" + i)
      //val meshes: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes/ .stl")).get
      ui.show(targetGroup2, meshes, "AlignedMeshes")
      val dataset = Seq(meshes)
      //
      val dc = DataCollection.fromTriangleMesh3DSequence(reference, dataset)
      val modelFromDataCollection = PointDistributionModel.createUsingPCA(dc)
      //
      //    ui.show(targetGroup3, modelFromDataCollection, "ModelDC")

    }*/
    val alignedmeshesFile = new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes").listFiles
    val alignedMeshes= alignedmeshesFile.map{MeshIO.readMesh(_).get}.toSeq
    alignedMeshes.map{mesh=>ui.show(targetGroup3,mesh,"M")}
    println("can we get here1")
    val reference: TriangleMesh[_3D] = alignedMeshes.head
    //val dc = DataCollection.fromTriangleMesh3DSequence(reference, dataset)
    val defFields = alignedMeshes.map{ m =>
      val deformationVectors = reference.pointSet.pointIds.map{ id : PointId =>
        m.pointSet.point(id) - reference.pointSet.point(id)
      }.toIndexedSeq
      DiscreteField3D(reference, deformationVectors)
    }
    val continuousFields = defFields.map(f => f.interpolate(TriangleMeshInterpolator3D()) )
    val gp = DiscreteLowRankGaussianProcess.createUsingPCA(reference,continuousFields, RelativeTolerance(1e-8) )
    val model = PointDistributionModel(gp)
    println("can we get here2")
    //val modelFromDataCollection = PointDistributionModel.createUsingPCA(dc)
    println("can we get here3")
    //
     ui.show(targetGroup3, model, "ModelDC")
    println("can we get here")


  }


}
