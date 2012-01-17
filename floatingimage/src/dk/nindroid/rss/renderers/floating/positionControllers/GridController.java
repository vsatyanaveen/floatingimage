package dk.nindroid.rss.renderers.floating.positionControllers;


public abstract class GridController extends PositionController {
	int mImageId;
	FeedDataProvider mDataProvider;
	
	public GridController(int imageId, FeedDataProvider dataProvider){
		this.mImageId = imageId;
		this.mDataProvider = dataProvider;
	}
	
	@Override
	public float adjustInterval(float interval){
		float spacing = 1.0f / (mDataProvider.getNumberOfImages() / 3.0f * 2);
		if(mImageId % 3 == 0){
			// Middle
			int row = mImageId * 2 / 3;
			return (interval - row * spacing + 1) % 1;
		}else{
			// Sides
			int row = (mImageId * 2 + 1) / 3;
			return (interval - row * spacing + 1) % 1;
		}
	}
}
