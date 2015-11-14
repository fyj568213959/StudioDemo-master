package cy.studiodemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import cy.studiodemo.R;
import cy.studiodemo.adapter.LeftMenuAdapter;
import cy.studiodemo.base.BaseActivity;
import cy.studiodemo.bean.CityModel;
import cy.studiodemo.bean.MenuItem;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.service.DownloadService;
import cy.studiodemo.fragment.ChatFragment;
import cy.studiodemo.fragment.DownloadAppFragment;
import cy.studiodemo.fragment.GroupBuyingFragment;
import cy.studiodemo.fragment.SettingFragment;
import cy.studiodemo.util.ToastUtil;
import cy.studiodemo.util.Utils;
import cy.studiodemo.view.BaseAnimatorSet;
import cy.studiodemo.view.CustomDelegate;
import cy.studiodemo.view.FadeExit;
import cy.studiodemo.view.FlipVerticalSwingEnter;
import cy.studiodemo.view.NormalDialog;
import cy.studiodemo.view.OnBtnLeftClickL;
import cy.studiodemo.view.OnBtnRightClickL;
import cy.studiodemo.view.SweetSheet;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private DownloadBroadcastReceiver mDownloadBroadcastReceiver;
    private RelativeLayout rl_main_content_root;
    private DrawerLayout drawerLayout;
    private ListView lv_left_menu;
    private SweetSheet mSweetSheet;
    private TextView tv_title_name;
    private TextView tv_menu_header_city;
    private ImageView iv_menu_header;
    private Button btn_title_menu;
    private Button bt_other;

    private long mExitTime;
    private int mPosition;
    private String mUserImageName = "userImage.jpg";//截取前的图片名称
    private String mFUserImageName = "fuserImage.jpg";//截取后的图片名称
    private File mFImageFile;
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private String[] mLeftMenus = {"团购", "app下载", "聊天", "设置"};

    // 照相的tag值
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    // 从相册取相片的tag值
    public static final int GET_PICTURE_FROM_XIANGCE_CODE = 2;
    // 取得裁剪后的照片
    public static final int GET_CUT_PICTURE_CODE = 3;
    //选择城市
    public static final int SELECT_CITY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLeftMenu();
        initFragments();
        registerDownloadReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterDownloadReceiver();
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void findViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        rl_main_content_root = (RelativeLayout) findViewById(R.id.rl_main_content_root);
        lv_left_menu = (ListView) findViewById(R.id.lv_left_menu);
        btn_title_menu = (Button) findViewById(R.id.btn_title_menu);
        tv_title_name = (TextView) findViewById(R.id.tv_title_name);
        bt_other = (Button) findViewById(R.id.bt_other);
    }

    @Override
    protected void setListensers() {
        btn_title_menu.setOnClickListener(this);
        bt_other.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE: // 系统相机
                if (resultCode == Activity.RESULT_OK) {
                    if (intent != null && intent.getData() != null) {
                        Utils.startPhotoZoom(this, intent.getData());
                    } else {
                        Uri uri = Uri.fromFile(new File(Utils.getCachePath(this), mUserImageName));
                        Utils.startPhotoZoom(this, uri);
                    }
                }
                break;
            case GET_PICTURE_FROM_XIANGCE_CODE: // 系统相册回调
                if (resultCode == Activity.RESULT_OK) {
                    Utils.startPhotoZoom(this, intent.getData());
                }
                break;
            case GET_CUT_PICTURE_CODE: // 剪后图片之后回调
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap bm = intent.getParcelableExtra("data");
                    mFImageFile = new File(Utils.getCachePath(this), mFUserImageName);
                    Utils.bmpToFile(bm, mFImageFile.getAbsolutePath());
                    iv_menu_header.setImageBitmap(Utils.getRoundedCornerBitmap(bm));
                }
                break;
            case SELECT_CITY: // 选择城市
                setCurrentCity();
                notifyCityChanged();
                break;
            default:
                break;
        }
    }

    private void initFragments() {
        mFragments.add(new GroupBuyingFragment());
        mFragments.add(new DownloadAppFragment());
        mFragments.add(new ChatFragment());
        mFragments.add(new SettingFragment());
        // 默认显示第一页
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!mFragments.get(0).isAdded()) {
            ft.add(R.id.content_frame, mFragments.get(0));
        }
        ft.commit();
    }

    // 初始化左边侧滑栏的顶部header
    private void initLeftMenu() {
        LayoutInflater localinflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View menuHeaderView = localinflater.inflate(R.layout.menu_header, null, false);
        tv_menu_header_city = (TextView) menuHeaderView.findViewById(R.id.tv_menu_header_city);
        iv_menu_header = (ImageView) menuHeaderView.findViewById(R.id.iv_menu_header);
        tv_menu_header_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSelectCityActivity();
            }
        });
        iv_menu_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                if (mSweetSheet == null) {
                    showSweetSheet();
                } else {
                    if (!mSweetSheet.isShow()) {
                        showSweetSheet();
                    }
                }
            }
        });
        setCurrentCity();
        lv_left_menu.addHeaderView(menuHeaderView);
        ArrayList<MenuItem> menuItems = getMenus();
        LeftMenuAdapter leftMenuAdapter = new LeftMenuAdapter(this, menuItems);
        lv_left_menu.setAdapter(leftMenuAdapter);
        lv_left_menu.setOnItemClickListener(new MyLeftMenuOnItemClickListener());
    }

    private void setCurrentCity() {
        CityModel cityModel = mMyApplication.getCurrentCity();
        tv_menu_header_city.setText("当前城市:" + cityModel.getCity_name());
    }

    private void notifyCityChanged() {
        GroupBuyingFragment groupBuyingFragment = (GroupBuyingFragment) mFragments.get(0);
        groupBuyingFragment.notifyCityChanged();
    }


    private void showSweetSheet() {
        mSweetSheet = new SweetSheet(rl_main_content_root);
        CustomDelegate customDelegate = new CustomDelegate(true, CustomDelegate.AnimationType.DuangLayoutAnimation);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_custom_view, null, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        customDelegate.setCustomView(view, layoutParams);
        mSweetSheet.setDelegate(customDelegate);
        view.findViewById(R.id.bt_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSweetSheet.dismiss();
                getoSystemCamera();
            }
        });
        view.findViewById(R.id.bt_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSweetSheet.dismiss();
                getoSystemPhoto();
            }
        });
        mSweetSheet.toggle();
    }

    private ArrayList getMenus() {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(mLeftMenus[0], R.mipmap.ic_dashboard));
        menuItems.add(new MenuItem(mLeftMenus[1], R.mipmap.ic_headset));
        menuItems.add(new MenuItem(mLeftMenus[2], R.mipmap.ic_forum));
        menuItems.add(new MenuItem(mLeftMenus[3], R.mipmap.ic_event));
        return menuItems;
    }

    private void setTitleName(int position) {
        switch (position) {
            case 0:
                tv_title_name.setText(mLeftMenus[0]);
                break;
            case 1:
                tv_title_name.setText(mLeftMenus[1]);
                break;
            case 2:
                tv_title_name.setText(mLeftMenus[2]);
                break;
            case 3:
                tv_title_name.setText(mLeftMenus[3]);
                break;
        }

    }

    public void switchContent(int position) {
        if (mPosition == position) {
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFragments.get(mPosition));
        mFragments.get(mPosition).setUserVisibleHint(false);
        mFragments.get(mPosition).onPause();
        if (mFragments.get(position).isAdded()) {
            ft.show(mFragments.get(position));
            mFragments.get(position).setUserVisibleHint(true);
            mFragments.get(position).onResume();
        } else {
            ft.add(R.id.content_frame, mFragments.get(position));
        }
        ft.commit();
        mPosition = position; // 更新目标tab为当前tab
    }

    /**
     * 调用系统的照相机
     */
    private void getoSystemCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Utils.getCachePath(this), mUserImageName);
        Uri mCameraUri = Uri.fromFile(file);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
        startActivityForResult(openCameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**
     * 从相册里面获取图片。
     */
    private void getoSystemPhoto() {
        Intent intent_pick = new Intent(Intent.ACTION_PICK, null);
        intent_pick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Utils.IMAGE_UNSPECIFIED);
        startActivityForResult(intent_pick, GET_PICTURE_FROM_XIANGCE_CODE);
    }

    /**
     * 跳转到选择城市
     */
    private void gotoSelectCityActivity() {
        Intent intent = new Intent(mContext, SelectCityActivity.class);
        startActivityForResult(intent, SELECT_CITY);
        overridePendingTransition(R.anim.push_right_in, R.anim.fade_out);
    }

    private void gotoOtherActivity() {
        ToastUtil.showToast(mContext, "do nothing.");
    }

    /**
     * 显示是否继续下载的dialog
     */
    private void showkeepOnDownloadDialog() {
        final NormalDialog dialog = new NormalDialog(mContext);
        BaseAnimatorSet bas_in = new FlipVerticalSwingEnter();
        BaseAnimatorSet bas_out = new FadeExit();
        dialog.content("当前有任务正在下载，是否在后台继续下载？")//
                .style(NormalDialog.STYLE_TWO)//
                .titleTextSize(23)//
                .btnText("后台下载", "停止下载")//
                .btnTextColor(Color.parseColor("#383838"), Color.parseColor("#D4D4D4"))//
                .btnTextSize(16f, 16f)//
                .showAnim(bas_in)//
                .dismissAnim(bas_out)//
                .show();

        dialog.setOnBtnLeftClickL(new OnBtnLeftClickL() {
            @Override
            public void onBtnLeftClick() {
                dialog.dismiss();
                setTitleName(0);
                switchContent(0);
            }
        });

        dialog.setOnBtnRightClickL(new OnBtnRightClickL() {
            @Override
            public void onBtnRightClick() {
                dialog.superDismiss();
                Intent intent = new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_DOWNLOAD_STOP);
                mMyApplication.startService(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                switch (mPosition) {
                    case 0:
                        if ((System.currentTimeMillis() - mExitTime) > 2000) {
                            ToastUtil.showToast(mContext, "再按一次返回键退出应用");
                            mExitTime = System.currentTimeMillis();
                        } else {
                            MainActivity.this.finish();
                        }
                        break;
                    case 1:
                        Intent intent = new Intent(mMyApplication, DownloadService.class);
                        intent.setAction(DownloadService.ACTION_DOWNLOAD_CHECK_ACTIVITY);
                        mMyApplication.startService(intent);
                        break;
                    case 2:
                        setTitleName(0);
                        switchContent(0);
                        break;
                    case 3:
                        setTitleName(0);
                        switchContent(0);
                        break;

                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_title_menu:
                if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.bt_other:
                gotoOtherActivity();
                break;
        }
    }

    // 注册广播接收者
    private void registerDownloadReceiver() {
        if (mDownloadBroadcastReceiver == null) {
            mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(GlobalParams.DOWNLOAD_APP_CHECK_ACTION);
            filter.addAction(GlobalParams.DOWNLOAD_APP_STOP_ACTION);
            registerReceiver(mDownloadBroadcastReceiver, filter);
        }
    }

    // 注销广播接收者
    private void unregisterDownloadReceiver() {
        if (mDownloadBroadcastReceiver != null) {
            try {
                unregisterReceiver(mDownloadBroadcastReceiver);
                mDownloadBroadcastReceiver = null;
            } catch (Exception e) {
            }
        }
    }

    /**
     * 接收下载的广播
     */
    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (GlobalParams.DOWNLOAD_APP_CHECK_ACTION.equals(action)) {
                boolean isAppDownload = intent.getBooleanExtra(GlobalParams.DOWNLOAD_APP_CHECK_KEY, false);
                if (isAppDownload) {
                    showkeepOnDownloadDialog();
                } else {
                    setTitleName(0);
                    switchContent(0);
                }
            } else if (GlobalParams.DOWNLOAD_APP_STOP_ACTION.equals(action)) {
                setTitleName(0);
                switchContent(0);
            }
        }
    }

    private class MyLeftMenuOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (position == 0) {                //点击listview头部返回
                return;
            }
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            setTitleName(position - 1);
            switchContent(position - 1);
        }
    }


}
