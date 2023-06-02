package ModelBuilding

import scalismo.geometry._3D
import scalismo.io.{MeshIO, StatismoIO}
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.statisticalmodel.dataset._
import scalismo.ui.api.ScalismoUI
import scalismo.utils.Random.implicits.randomGenerator

import java.awt.Color
import java.io.File

object SSM {
  def main(args: Array[String]): Unit ={
    scalismo.initialize()
    val ui = ScalismoUI()
    //loading of the scapula dataset
    //val reference: TriangleMesh = MeshIO.readMesh(new File("E:\\Rassire-project-07-07-2017\\SA-SW data-09-08-2017\\scapula\\meshes\\old files-12-10-2017\\right\\registered-meshes - 08-09-2017\\SA04-43M-LSFr.stl")).get
    //val dc1 = DataCollection.fromMeshDirectory(reference, new File("E:\\Rassire-project-07-07-2017\\SA-SW data-09-08-2017\\scapula\\meshes\\old files-12-10-2017\\right\\registered-meshes - 08-09-2017\\"))._1.get

    //loading of the humeral datasets
    //val reference : TriangleMesh = MeshIO.readMesh(new File("E:\\Master folders\\Draft of the journal paper\\SA data for the paper\\scapula\\registered scapula\\12-14LS_fr.stl")).get
    //val dc1 = DataCollection.fromMeshDirectory(reference, new File("E:\\Master folders\\Draft of the journal paper\\SA data for the paper\\scapula\\registered scapula\\"))._1.get
    //val reference = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/Tibia_ref.stl")).get
    val reference = MeshIO.readMesh(new File("C:\\Users\\NMYCAT001\\Documents\\NMYCAT001\\DataGeneration\\Foot\\vertebra2\\data\\Fibula_20062022\\Fibula\\reference\\CT2_Fibula.stl")).get
    //val reference = MeshIO.readMesh(new File("C:\\Users\\NMYCAT001\\Documents\\NMYCAT001\\DataGeneration\\Foot\\vertebra2\\data\\Fibula_20062022\\Fibula\\registered-fine\\triangle-meshes\\CT2.stl")).get

    print("I have read the reference")
    val modelGroup1 = ui.createGroup("Reference")
    val meshView = ui.show(modelGroup1, reference, "Mesh")
    // change its color
    meshView.color = Color.PINK
    //val meshFiles = new java.io.File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes-Remeshed/").listFiles
    val AlignMeshes = (1 until 11).map { i: Int =>
      //val Meshes:TriangleMesh[_3D] =MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes-Remeshed/")).get
      val Meshes: TriangleMesh[_3D] = MeshIO.readMesh(new File("C:\\Users\\NMYCAT001\\Documents\\NMYCAT001\\DataGeneration\\Foot/vertebra2\\data\\Fibula_20062022\\Fibula\\registered-fine\\triangle-meshes\\CT"+i+".stl")).get
      //val Meshes: TriangleMesh[_3D] = MeshIO.readMesh(new File("C/Users/NMYCAT001/Documents/NMYCAT001/DataGeneration/Foot/vertebra2/data/Fibula_20062022/Fibula/registered-fine/triangle-meshes/CT"+i+".stl")).get
      ui.show(Meshes,"meshes")
      print("I have read the dataset")
      val alignedData = Seq(Meshes)
      val dc = DataCollection.fromTriangleMesh3DSequence(reference, alignedData)
      //val dc2 = DataCollection.fromMeshDirectory(reference,new File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes-Remeshed/"))._1.get

      //
      //    //val dc=DataCollection(dc1)
      val dcWithGPAAlignedShapes = DataCollection.gpa(dc)
      val modelFromDataCollection = PointDistributionModel.createUsingPCA(dcWithGPAAlignedShapes)

      ui.show(modelFromDataCollection,"PCA_model2")
      //
      //    val modelGroup2 = ui.createGroup("modelGroup2")
      //    ui.show(modelGroup2, modelFromDataCollection, "ModelDC")

      print("Fin")
      //saving of the scapula pca model
      //StatismoIO.writeStatismoMeshModel(model, new File("E:\\project\\Methodolgy\\Training_Dataset_generation\\basel-face-pipeline-master-felix\\pipeline-data\\recognition-experiment\\Scapula_SSModel\\My_scapula_model.h5"))
      // ui.show(model.sample,"PCA_model2")
      // ui.show(model.sample,"PCA_model3")


    }
  }

}
