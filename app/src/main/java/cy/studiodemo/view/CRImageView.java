package cy.studiodemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * @author zzz40500
 * @version 1.0
 * @date 2015/8/8.
 * @github: https://github.com/zzz40500
 */
public class CRImageView extends android.widget.ImageView {


    private CircleRevealHelper mCircleRevealHelper;


    public CRImageView(Context context) {
        super(context);
        init();
    }


    public CRImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CRImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (getParent() instanceof GrowUpParent) {
            return ((GrowUpParent) getParent()).onParentHandMotionEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void init() {

        mCircleRevealHelper = new CircleRevealHelper(this);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        mCircleRevealHelper.onDraw(canvas);
    }


}
