package Tetra_meshes

import scalismo.io.{ImageIO, MeshIO}
import scalismo.ui.api.ScalismoUI

import java.io.File

object Read_ScalarVolumeMesh {
  def main(args: Array[String]):Unit = {

    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)
    val targetGroup = ui.createGroup("target")

    //val ScalarVolumeMesh= MeshIO.readScalarVolumeMeshField[Float](new File("C:\\Users\\NMYCAT001\\Documents\\NMYCAT001\\DataGeneration\\june_trial\\data\\Objects\\Left\\ReferenceMesh\\volumes\\CT2_Tibia.vtk")).get

    val CT_image =  ImageIO.read3DScalarImage[Short](new File("data//CT_Images//CT_images//CT11_HindShinFoot.nii")).get.map(_.toInt)//C:\Users\NMYCAT001\Documents\NMYCAT001\DataGeneration\june_trial\data\CT_Images   CT_Images\\CT_images\\CT"+i+"_MidForeFoot.nii

    //val newMesh = ScalarVolumeMesh
    //ui.show(targetGroup, ScalarVolumeMesh, "ScalarVolume")
    ui.show(targetGroup,CT_image,"CT_image")
    ImageIO.writeNifti(CT_image, new File("data//CT_Images//CT_images//CT11_HindShinFoot2.nii"))

  }

}
