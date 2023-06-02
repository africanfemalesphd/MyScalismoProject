package ObjectAlignment

import scalismo.geometry.{Landmark, Point, Point3D, _3D}
import scalismo.io.{LandmarkIO, MeshIO}
import scalismo.mesh.TriangleMesh
import scalismo.registration.LandmarkRegistration
import scalismo.ui.api.ScalismoUI

import java.io.File

object Metatarsal_2_Alignment {

  def main(args: Array[String])={


    val ui = ScalismoUI()
    scalismo.initialize()
    implicit val rng = scalismo.utils.Random(42)

    val targetGroup = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")
    val targetGroup3 = ui.createGroup("target3")

    val referenceMesh = MeshIO.readMesh(new File("data/CT_Images/CT2/CT2_2_Metatarsal.stl")).get
    val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/CT_Images/CT2/CT2_2_Metatarsal.json")).get
    ui.show(targetGroup, referenceMesh, "referenceMesh")
    ui.show(targetGroup, refLmks, "referenceLandmarks")

    val referenceCentre = Point3D(referenceMesh.boundingBox.oppositeCorner(0) - referenceMesh.boundingBox.origin(0).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(1) - referenceMesh.boundingBox.origin(1)).round.toInt,
      (referenceMesh.boundingBox.oppositeCorner(2) - referenceMesh.boundingBox.origin(2)).round.toInt
    )


    //load femur and mesh plus their landmarks

    //Load btwn 0-50 meshes using the for loop
    val AlignMeshes = (1 until 12).map { i: Int =>
      val mesh: TriangleMesh[_3D] = MeshIO.readMesh(new File("data/Objects/Left/Forefoot/2_Metatarsal/initial/triangle-meshes/CT"+i+"_2_Metatarsal.stl")).get
      //ui.show(targetGroup2, mesh, "Unaligned_FemurMeshes" + i)

      val UnalignedMeshLmks = LandmarkIO.readLandmarksJson[_3D](new File("data/Objects/Left/Forefoot/2_Metatarsal/initial/landmarks/CT"+i+"_2_Metatarsal.json")).get
      //ui.show(targetGroup2, UnalignedMeshLmks, "Unaligned_FemurLandmarks" + i)


      //aligment mesh landmarks to the femur landmarks
      val bestTransform = LandmarkRegistration.rigid3DLandmarkRegistration(UnalignedMeshLmks, refLmks, referenceCentre)
      // How to i get to convert points to landmarks and show them in UI
      val TranslatedPoints: Seq[Point[_3D]] = UnalignedMeshLmks.map(lm => bestTransform(lm.point))
      val TranslatedPointsToLmks: Seq[Landmark[_3D]] = TranslatedPoints.zipWithIndex.map { p => Landmark(p._2.toString, p._1) }
      //val LmksAlignedShow = ui.show(targetGroup3,TranslatedPointsToLmks,"points")
      ui.show(targetGroup3, TranslatedPointsToLmks, "points")

      val AlignedFemur: TriangleMesh[_3D] = mesh.transform(bestTransform)
      ui.show(targetGroup3, AlignedFemur, "AlignedFibula" + i)

      //How to save the results in a folder
      MeshIO.writeMesh(AlignedFemur,new File("data/Objects/Left/Forefoot/2_Metatarsal/aligned/triangle-meshes/CT"+i+".stl"))

      LandmarkIO.writeLandmarksJson[_3D](TranslatedPointsToLmks,new File("data/Objects/Left/Forefoot/2_Metatarsal/aligned/landmarks/CT"+i+".json"))


    }


  }
  println("Oui Fini")

}
