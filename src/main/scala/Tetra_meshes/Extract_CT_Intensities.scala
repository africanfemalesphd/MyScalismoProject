package Tetra_meshes

import scalismo.common.interpolation.BSplineImageInterpolator3D
import scalismo.geometry._3D
import scalismo.image.DiscreteImage
import scalismo.image.filter.DiscreteImageFilter
import scalismo.io.{ImageIO, MeshIO}
import scalismo.mesh.{ScalarVolumeMeshField, TetrahedralMesh}
import scalismo.ui.api.ScalismoUI

import java.io.File

object Extract_CT_Intensities {
  def main(args:Array[String]): Unit = {

    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)
    val targetGroup = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")

    val CT_image =  ImageIO.read3DScalarImage[Int](new File("data//CT_Images//CT1.nii")).get.map(_.toShort)//C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\CT_Images
    ui.show(targetGroup, CT_image, "CT")
    //val tetra = MeshIO.readTetrahedralMesh(new File("data//CT_Images//CT2_remeshed.vtu")).get

    //val tetra = MeshIO.readTetrahedralMesh(new File("data/Objects/Left/Shin_Bones/Tibia/Inverse_aligned/tetrahedral-meshes/CT5_Tibia.vtu")).get
    val tetra = MeshIO.readTetrahedralMesh(new File("data/Objects/Left/Shin_Bones/Fibula/Inverse_aligned/tetrahedral-meshes/CT1_Fibula.vtu")).get
    //val tetra = MeshIO.readTetrahedralMesh(new File("data/Objects/Left/Hindfoot/Talus/Inverse_aligned/tetrahedral-meshes/CT1_Talus.vtu")).get
    //val tetra = MeshIO.readTetrahedralMesh(new File("data/Objects/Left/Hindfoot/Calcaneus/Inverse_aligned/tetrahedral-meshes/CT5_Calcaneus.vtu")).get

    ui.show(targetGroup,tetra, "Tetra")

    // val refland1=LandmarkIO.readLandmarksJson[_3D](new File("E:\\PhD folders\\Private data\\knee (American data)\\test one femur\\Synth9_22_0(ref1).json")).get

    //val image =ImageIO.read3DScalarImage[Short](new File("E:\\Colleague work to assist\\Yvonne\\02.nii")).get
    //val TargetMesh = MeshIO.readMesh(new File("data//CT_Images//CT2_Tibia_remeshed.stl")).get //C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\Objects\Left\Shin_Bones\Tibia\initial\triangle-meshes

    //val TargetMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Tibia/Inverse_aligned/triangle-meshes/CT5_Tibia.stl")).get //C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\Objects\Left\Shin_Bones\Tibia\initial\triangle-meshes
    val TargetMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Fibula/Inverse_aligned/triangle-meshes/CT1_Fibula.stl")).get //C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\Objects\Left\Shin_Bones\Tibia\initial\triangle-meshes
    //val TargetMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/Inverse_aligned/triangle-meshes/CT1_Talus.stl")).get //C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\Objects\Left\Shin_Bones\Tibia\initial\triangle-meshes
    //val TargetMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Calcaneus/Inverse_aligned/triangle-meshes/CT5_Calcaneus.stl")).get //C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\Objects\Left\Shin_Bones\Tibia\initial\triangle-meshes
    ui.show(targetGroup2,TargetMesh,"Mesh")

    val volumetricScalarMesh=getIntensity(tetra,CT_image)

    ui.show(volumetricScalarMesh,"scalar mesh")

  }
  def getIntensity(tetrahedral: TetrahedralMesh[_3D],  CT_image: DiscreteImage[_3D, Short])={
    val discreteTargetImage = DiscreteImageFilter.gaussianSmoothing(
      CT_image,
      3.0
    )

    val targetImage = discreteTargetImage.interpolate(BSplineImageInterpolator3D[Short](3))

    val data=tetrahedral.pointSet.points.toIndexedSeq.map(p=>if (targetImage.domain.isDefinedAt(p)) targetImage(p) else (- 1024.0 ).toShort)
    ScalarVolumeMeshField(tetrahedral,data)//.interpolate(NearestNeighborInterpolator())
    //MeshIO.writeScalarMeshField(data, new File("")

  }


}
