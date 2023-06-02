package CT_Transformation

import breeze.linalg.DenseVector
import scalismo.color.RGB
import scalismo.common.Field
import scalismo.common.interpolation.{BSplineImageInterpolator3D, NearestNeighborInterpolator}
import scalismo.geometry.{EuclideanVector, Point, Point3D, _3D}
import scalismo.image.{DiscreteImage, DiscreteImageDomain, DiscreteImageDomain3D}
import scalismo.io.{ImageIO, LandmarkIO, MeshIO, StatismoIO}
import scalismo.mesh.TriangleMesh
import scalismo.registration.LandmarkRegistration
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.transformations.TranslationAfterRotation
import scalismo.ui.api.ScalismoUI
import scalismo.utils.Random

import java.awt.Color
import java.io.File

object ForKet {
  def main(args: Array[String]): Unit = {
    scalismo.initialize()
    val ui = ScalismoUI()

    val targetGroup1 = ui.createGroup("target")
    val targetGroup2 = ui.createGroup("target2")
    val targetGroup3 = ui.createGroup("target3")

    implicit val rng = Random(1024l)
    val Ref_CT_image = ImageIO.read3DScalarImage[Short](new File("C:\\Users\\NMYCAT001\\Documents\\PhD\\june_trial\\data\\CT_Images\\CT_images\\CT2_HindShinFoot.nii")).get //.map(_.toShort)
    val refLmks = LandmarkIO.readLandmarksJson[_3D](new File("C:\\Users\\NMYCAT001\\Documents\\PhD\\june_trial\\data\\CT_Images\\CT_images\\Lmks\\CT2_image_Lmk.json")).get

    val referenceCentre = Point3D(Ref_CT_image.domain.boundingBox.oppositeCorner(0) - Ref_CT_image.domain.boundingBox.origin(0).round.toInt,
      (Ref_CT_image.domain.boundingBox.oppositeCorner(1) - Ref_CT_image.domain.boundingBox.origin(1)).round.toInt,
      (Ref_CT_image.domain.boundingBox.oppositeCorner(2) - Ref_CT_image.domain.boundingBox.origin(2)).round.toInt
    )

    val CT: DiscreteImage[_3D, Short] = ImageIO.read3DScalarImage[Short](new File("C:\\Users\\NMYCAT001\\Documents\\PhD\\june_trial\\data\\CT_Images\\CT_images\\CT1_HindShinFoot.nii")).get
    //val CTinterpolated = CT.interpolate(NearestNeighborInterpolator())
    val UnalignedMeshLmks = LandmarkIO.readLandmarksJson[_3D](new File("C:\\Users\\NMYCAT001\\Documents\\PhD\\june_trial\\data\\CT_Images\\CT_images\\Lmks\\CT1_image_Lmk.json")).get

    val bestTransform: TranslationAfterRotation[_3D] = LandmarkRegistration.rigid3DLandmarkRegistration(UnalignedMeshLmks, refLmks, referenceCentre)
    def volumeTransformation(CT: DiscreteImage[_3D, Short], transformationMatrix: TranslationAfterRotation[_3D]):DiscreteImage[_3D, Short]={
      val CTinterpolated: Field[_3D, Short] = CT.interpolate(NearestNeighborInterpolator())
      val newDomain = DiscreteImageDomain3D(
        CT.domain.boundingBox,
        EuclideanVector(1.0, 1.0, 1.0)
      )
//    val newDomain = DiscreteImageDomain3D(
//      CT.domain.boundingBox,
//      EuclideanVector(1.0, 1.0, 1.0)
//    )
    // new axis aligned discretization, compute axis aligend boundingbox of old bounding box
    //val newValues = newDomain.pointSet.points.map{ newLocation => CTinterpolated(bestTransform.inverse(newLocation))}
    val newValues = newDomain.pointSet.points.map{ newLocation => CTinterpolated(transformationMatrix.inverse(newLocation))}
      DiscreteImage[_3D](newDomain, newValues)
    }
    //val TranslatedCT: DiscreteImage[_3D, Short] = DiscreteImage[_3D](newDomain, newValues)
    val TranslatedCT: DiscreteImage[_3D, Short] = volumeTransformation(CT, bestTransform)


    ui.show(targetGroup3, TranslatedCT, "AlignedFibula" )
    ImageIO.writeNifti(TranslatedCT, new File("C:\\Users\\NMYCAT001\\Documents\\PhD\\june_trial\\data\\CT_Images\\CT_images\\CT_images_Transformed\\New_HindShinFootAligned.nii"))
  }
}
