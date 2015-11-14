package cy.studiodemo.fragment;

import android.view.View;

import cy.studiodemo.R;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.view.ScrollingImageView;

/**
 * Created by cuiyue on 15/7/27.
 */
public class ChatFragment extends BaseFragment {

    private ScrollingImageView scrollingBackground;

    @Override
    protected void initVariable() {
        scrollingBackground.stop();
        scrollingBackground.start();
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void findViews(View rootView) {
        scrollingBackground = (ScrollingImageView) rootView.findViewById(R.id.scrolling_background);
    }

    @Override
    protected void setListensers() {

    }
}
