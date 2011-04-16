package dk.nindroid.rss.renderers.floating.positionControllers;


public abstract class SequentialController extends PositionController {
	int mImageId;
	int mNoImages;
	
	public SequentialController(int imageId, int noImages){
		this.mImageId = imageId;
		this.mNoImages = noImages;
	}
	
	public float adjustInterval(float interval){
		float spacing = 1.0f / mNoImages;
		return ((interval - mImageId * spacing) + 1) % 1;
	}
}
