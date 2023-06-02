package ObjectAlignment
/*Rigid alignment is performed as part of any data preparation process before model building.
Rigid alignment helps to normalize the pose of any object with respect to the reference
 */

import scalismo.geometry.{Landmark, Point, Point3D, _3D}
import scalismo.io.{LandmarkIO, MeshIO}
import scalismo.mesh.TriangleMesh
import scalismo.registration.LandmarkRegistration
import scalismo.ui.api.ScalismoUI

import java.io.File

object RigidAlignment {
  def main(args: Array[String]): Unit = {
    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)

    val targetGroup = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")
    val targetGroup3 = ui.createGroup("target3")

    val referenceMesh = MeshIO.readMesh(new File("data/Objects/Left/ReferenceMesh/CT01_Segmentation_Tibia_Left.ply")).get
    val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/ReferenceLmks/CT01_Segmentation_Tibia_Left.json")).get
    ui.show(targetGroup, referenceMesh, "referenceMesh")
    ui.show(targetGroup, refLmks, "referenceLandmarks")

    val referenceCentre = Point3D(referenceMesh.boundingBox.oppositeCorner(0) - referenceMesh.boundingBox.origin(0).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(1) - referenceMesh.boundingBox.origin(1)).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(2) - referenceMesh.boundingBox.origin(2)).round.toInt
    )


    //load femur and mesh plus their landmarks

    //Load btwn 0-50 meshes using the for loop
    val AlignMeshes = (1 until 9).map { i: Int =>
      val mesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Tibia/initial/triangle-meshes/CT0"+ i +"_Segmentation_Tibia_Left.ply")).get
      //ui.show(targetGroup2, mesh, "Unaligned_FemurMeshes" + i)

      val UnalignedMeshLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Tibia/initial/landmarks/CT0"+ i +"_Segmentation_Tibia_Left.json")).get
      //ui.show(targetGroup2, UnalignedMeshLmks, "Unaligned_FemurLandmarks" + i)


      //aligment mesh landmarks to the femur landmarks
      val bestTransform = LandmarkRegistration.rigid3DLandmarkRegistration(UnalignedMeshLmks, refLmks, referenceCentre)
      // How to i get to convert points to landmarks and show them in UI
      val TranslatedPoints: Seq[Point[_3D]] = UnalignedMeshLmks.map(lm => bestTransform(lm.point))
      val TranslatedPointsToLmks: Seq[Landmark[_3D]] = TranslatedPoints.zipWithIndex.map { p => Landmark(p._2.toString, p._1) }
      //val LmksAlignedShow = ui.show(targetGroup3,TranslatedPointsToLmks,"points")
      ui.show(targetGroup3, TranslatedPointsToLmks, "points")

      val AlignedFemur: TriangleMesh[_3D] = mesh.transform(bestTransform)
      ui.show(targetGroup3, AlignedFemur, "AlignedTibias" + i)

      //How to save the results in a folder
      MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Tibia/aligned/triangle-meshes/CT"+i+"Aligned_Tibia_Left.stl"))

      LandmarkIO.writeLandmarksJson[_3D](TranslatedPointsToLmks,new File("data/Objects/Left/Tibia/aligned/landmarks/CT"+i+"AlignedLmks_Tibia_Left.json"))


    }


  }
  println("Oui Fini")

}
