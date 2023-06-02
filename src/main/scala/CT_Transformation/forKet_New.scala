import breeze.linalg.DenseVector
import scalismo.color.RGB
import scalismo.common.Field
import scalismo.common.interpolation.NearestNeighborInterpolator
import scalismo.geometry.{EuclideanVector, Point, Point3D, _3D}
import scalismo.image.{DiscreteImage, DiscreteImage3D, DiscreteImageDomain, DiscreteImageDomain3D}
import scalismo.io.{ImageIO, MeshIO, StatismoIO}
import scalismo.mesh.TriangleMesh
import scalismo.registration.LandmarkRegistration
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.transformations.{Transformation, TranslationAfterRotation}
import scalismo.utils.Random

import java.awt.Color
import java.io.File

object ForKet {
  def main(args: Array[String]): Unit = {
    scalismo.initialize()
    implicit val rng = Random(1024l)
    val CT: DiscreteImage[_3D, Short] = ImageIO.read3DScalarImage[Short](new File("data/CT2_cropped.nii")).get
    val bestTransform:TranslationAfterRotation[_3D] = ???
    def volumeTransformation(CT_image: DiscreteImage[_3D, Short], transformationMatrix: TranslationAfterRotation[_3D]):DiscreteImage[_3D, Short]={
      val CTinterpolated: Field[_3D, Short] = CT.interpolate(NearestNeighborInterpolator())
      val newDomain = DiscreteImageDomain3D(
        CT.domain.boundingBox,
        EuclideanVector(1.0, 1.0, 1.0)
      )
      // new axis aligned discretization, compute axis aligend boundingbox of old bounding box
      val newValues = newDomain.pointSet.points.map{ newLocation => CTinterpolated(transformationMatrix.inverse(newLocation))}
      DiscreteImage[_3D](newDomain, newValues)
    }
    val TranslatedCT: DiscreteImage[_3D, Short] = volumeTransformation(CT, bestTransform)
    ImageIO.writeNifti(TranslatedCT, new File("data/CT2_cropped_transformed.nii"))
  }
}
