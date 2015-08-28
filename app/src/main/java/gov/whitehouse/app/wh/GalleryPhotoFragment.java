package gov.whitehouse.app.wh;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.BaseFragment;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.util.GsonUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

public
class GalleryPhotoFragment extends BaseFragment
{

    public static final String EXTRA_FEED_ITEM = "extra:feed_item";

    public static final String EXTRA_INDEX = "extra:index";

    private
    int mIndexInGallery;

    private
    boolean mTouching;

    private
    boolean mFavorited;

    private
    FeedItem mFeedItem;

    private
    Menu mMenu;

    private
    PhotoViewAttacher mPhotoViewAttacher;

    @InjectView(R.id.photo)
    ImageView mPhotoView;

    @InjectView(R.id.photo_description)
    TextView mPhotoDescription;

    public static
    GalleryPhotoFragment newInstance(FeedItem item, int indexInGallery)
    {
        GalleryPhotoFragment f = new GalleryPhotoFragment();
        Bundle args = new Bundle(2);
        args.putString(EXTRA_FEED_ITEM, GsonUtils.toJson(item));
        args.putInt(EXTRA_INDEX, indexInGallery);
        f.setArguments(args);
        return f;
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Bundle args = getArguments();
        mFeedItem = GsonUtils.fromJson(args.getString(EXTRA_FEED_ITEM), FeedItem.class); /* TODO: Make FeedItem Parcelable */
        mIndexInGallery = args.getInt(EXTRA_INDEX, -1);
    }

    @Override
    public
    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_gallery_photo, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    private
    String prepDescription(String original)
    {
        return original.substring(original.lastIndexOf("<p>") + 3, original.lastIndexOf("</p>"));
    }

    private
    void decideDescriptionVisibility(float scale)
    {
        if (scale > 1.0f) {
            if (!mTouching) {
                mPhotoDescription.animate()
                        .alpha(0.0f)
                        .start();
            }
        } else {
            if (!mTouching) {
                mPhotoDescription.animate()
                        .alpha(1.0f)
                        .start();
            }
        }
    }

    @Override
    public
    void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mPhotoDescription.setText(Html.fromHtml(prepDescription(mFeedItem.description())));
        Picasso.with(view.getContext())
               .load(mFeedItem.getBestThumbnailUrl(1500))
               .into(mPhotoView, new Callback()
               {
                   @Override
                   public
                   void onSuccess()
                   {
                       mPhotoViewAttacher = new PhotoViewAttacher(mPhotoView) {
                           @Override
                           public
                           boolean onTouch(View v, MotionEvent ev)
                           {
                               switch (ev.getAction()) {
                                   case MotionEvent.ACTION_DOWN:
                                       mTouching = true;
                                       decideDescriptionVisibility(getScale());
                                       break;
                                   case MotionEvent.ACTION_CANCEL:
                                   case MotionEvent.ACTION_UP:
                                       mTouching = false;
                                       decideDescriptionVisibility(getScale());
                                       break;
                               }
                               return super.onTouch(v, ev);
                           }
                       };
                       mPhotoViewAttacher.setOnMatrixChangeListener(rectF -> {
                           decideDescriptionVisibility(mPhotoViewAttacher.getScale());
                           if (mPhotoViewAttacher.getScale() > 1.1f) {
                               mPhotoViewAttacher.setAllowParentInterceptOnEdge(false);
                           } else {
                               mPhotoViewAttacher.setAllowParentInterceptOnEdge(true);
                           }
                       });
                   }

                   @Override
                   public
                   void onError()
                   {
                   }
               });
    }

    @Override
    public
    void onDestroyView()
    {
        if (mPhotoViewAttacher != null) {
            mPhotoViewAttacher.cleanup();
            mPhotoViewAttacher = null;
        }
        super.onDestroyView();
    }

    @Override
    public
    boolean isRootFragment()
    {
        return false;
    }
}
