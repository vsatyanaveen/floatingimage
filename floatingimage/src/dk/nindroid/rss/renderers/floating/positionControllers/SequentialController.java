package dk.nindroid.rss.renderers.floating.positionControllers;



public abstract class SequentialController extends PositionController {
	int mImageId;
	FeedDataProvider dataProvider;
	
	public SequentialController(int imageId, FeedDataProvider dataProvider){
		this.mImageId = imageId;
		this.dataProvider = dataProvider;
	}
	
	public float adjustInterval(float interval){
		float spacing = 1.0f / dataProvider.getNumberOfImages();
		return ((interval - mImageId * spacing) + 1) % 1;
	}
}
