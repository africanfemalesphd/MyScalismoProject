package Object_InverseAlignment

import scalismo.geometry.{Landmark, Point, Point3D, _3D}
import scalismo.io.{LandmarkIO, MeshIO}
import scalismo.mesh.TriangleMesh
import scalismo.registration.LandmarkRegistration
import scalismo.ui.api.ScalismoUI

import java.awt.Color
import java.io.File

object Inverse_Alignment {

  def main(args: Array[String]): Unit ={
    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)

    val targetGroup = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")
    val targetGroup3 = ui.createGroup("target3")

    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Tibia/initial/triangle-meshes/CT1_Tibia.stl")).get
    //val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Shin_Bones/Tibia/initial/landmarks/CT1_Tibia.json")).get

    val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Fibula/initial/triangle-meshes/CT4_Fibula.stl")).get
    val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Shin_Bones/Fibula/initial/landmarks/CT4_Fibula.json")).get

   //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/initial/triangle-meshes/CT1_Talus.stl")).get
   //val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Hindfoot/Talus/initial/landmarks/CT1_Talus.json")).get

    //val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Calcaneus/initial/triangle-meshes/CT5_Calcaneus.stl")).get
    //val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Hindfoot/Calcaneus/initial/landmarks/CT5_Calcaneus.json")).get
    ui.show(targetGroup, referenceMesh, "referenceMesh")
    ui.show(targetGroup, refLmks, "referenceLandmarks")


    val referenceCentre = Point3D(referenceMesh.boundingBox.oppositeCorner(0) - referenceMesh.boundingBox.origin(0).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(1) - referenceMesh.boundingBox.origin(1)).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(2) - referenceMesh.boundingBox.origin(2)).round.toInt
    )
    //val targetMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Tibia/aligned/triangle-meshes-Remeshed/CT1_Tibia.stl")).get
    //val targetLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Shin_Bones/Tibia/aligned/landmarks_Renamed/CT1_Aligned_Tibia.json")).get

    val targetMesh = MeshIO.readMesh(new File("data/Objects/Left/Shin_Bones/Fibula/aligned/triangle-meshes-Remeshed/CT4_Fibula.stl")).get
    val targetLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Shin_Bones/Fibula/aligned/landmarks_Renamed/CT4_Aligned_Fibula.json")).get

    //val targetMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Talus/aligned/triangle-meshes-Remeshed/CT1_Talus.stl")).get
    //val targetLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Hindfoot/Talus/aligned/landmarks_Renamed/CT1_Aligned_Talus.json")).get

    //val targetMesh = MeshIO.readMesh(new File("data/Objects/Left/Hindfoot/Calcaneus/aligned/triangle-meshes-Remeshed/CT5_Calcaneus.stl")).get
    //val targetLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Hindfoot/Calcaneus/aligned/landmarks_Renamed/CT5_Aligned_Calcaneus.json")).get

    ui.show(targetGroup2, targetMesh, "TargetMesh")
    ui.show(targetGroup2, targetLmks, "TargetLandmarks")


    //load femur and mesh plus their landmarks

    //Load btwn 0-50 meshes using the for loop

      //aligment mesh landmarks to the femur landmarks
      val bestTransform = LandmarkRegistration.rigid3DLandmarkRegistration(targetLmks, refLmks, referenceCentre)
      // How to i get to convert points to landmarks and show them in UI
      val TranslatedPoints: Seq[Point[_3D]] = targetLmks.map(lm => bestTransform(lm.point))
      val TranslatedPointsToLmks: Seq[Landmark[_3D]] = TranslatedPoints.zipWithIndex.map { p => Landmark(p._2.toString, p._1) }
      //val LmksAlignedShow = ui.show(targetGroup3,TranslatedPointsToLmks,"points")
      ui.show(targetGroup3, TranslatedPointsToLmks, "points")

      val AlignedFemur: TriangleMesh[_3D] = targetMesh.transform(bestTransform)
      val new_mesh = ui.show(targetGroup3, AlignedFemur, "Inverse_Aligned" )
     new_mesh.color = Color.PINK

      //How to save the results in a folder
      //MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Shin_Bones/Tibia/Inverse_aligned/triangle-meshes/CT1_Tibia.stl"))
      MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Shin_Bones/Fibula/Inverse_aligned/triangle-meshes/CT4_Fibula.stl"))
      //MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Hindfoot/Talus/Inverse_aligned/triangle-meshes/CT1_Talus.stl"))
      //MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Hindfoot/Calcaneus/Inverse_aligned/triangle-meshes/CT5_Calcaneus.stl"))




    //LandmarkIO.writeLandmarksJson[_3D](TranslatedPointsToLmks,new File("data/Objects/Left/Shin_Bones/Fibula/aligned/landmarks/CT1_Fibula.json"))


    }

}
