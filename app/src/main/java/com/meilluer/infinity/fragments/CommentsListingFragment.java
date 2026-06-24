package com.meilluer.infinity.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import com.meilluer.infinity.Infinity;
import com.meilluer.infinity.NetworkState;
import com.meilluer.infinity.R;
import com.meilluer.infinity.RecyclerViewContentScrollingInterface;
import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.activities.BaseActivity;
import com.meilluer.infinity.adapters.CommentsListingRecyclerViewAdapter;
import com.meilluer.infinity.comment.Comment;
import com.meilluer.infinity.comment.CommentViewModel;
import com.meilluer.infinity.customtheme.CustomThemeWrapper;
import com.meilluer.infinity.customviews.AdjustableTouchSlopItemTouchHelper;
import com.meilluer.infinity.customviews.LinearLayoutManagerBugFixed;
import com.meilluer.infinity.databinding.FragmentCommentsListingBinding;
import com.meilluer.infinity.events.ChangeNetworkStatusEvent;
import com.meilluer.infinity.thing.ReplyNotificationsToggle;
import com.meilluer.infinity.thing.SortType;
import com.meilluer.infinity.utils.SharedPreferencesUtils;
import com.meilluer.infinity.utils.Utils;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;
import com.meilluer.infinity.archive.ArcticShiftUserListingFetcher;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_USERNAME = "EN";
    public static final String EXTRA_ARE_SAVED_COMMENTS = "EISC";

    CommentViewModel mCommentViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor mExecutor;
    private RequestManager mGlide;
    private BaseActivity mActivity;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private CommentsListingRecyclerViewAdapter mAdapter;
    private SortType sortType;
    private ColorDrawable backgroundSwipeRight;
    private ColorDrawable backgroundSwipeLeft;
    private Drawable drawableSwipeRight;
    private Drawable drawableSwipeLeft;
    private int swipeLeftAction;
    private int swipeRightAction;
    private float swipeActionThreshold;
    private AdjustableTouchSlopItemTouchHelper touchHelper;
    private boolean shouldSwipeBack;
    private FragmentCommentsListingBinding binding;

    public CommentsListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentsListingBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        EventBus.getDefault().register(this);

        applyTheme();

        mGlide = Glide.with(mActivity);

        if (mActivity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(mActivity.getWindow().getDecorView(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );
                    binding.recyclerViewCommentsListingFragment.setPadding(
                            0, 0, 0, allInsets.bottom
                    );
                    return insets;
                }
            });
            //binding.recyclerViewCommentsListingFragment.setPadding(0, 0, 0, mActivity.getNavBarHeight());
        }/* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewCommentsListingFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }*/

        boolean enableSwipeAction = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false);
        boolean vibrateWhenActionTriggered = mSharedPreferences.getBoolean(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED, true);
        swipeActionThreshold = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD, "0.3"));
        swipeRightAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_RIGHT_ACTION, "1"));
        swipeLeftAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_LEFT_ACTION, "0"));
        initializeSwipeActionDrawable();
        touchHelper = new AdjustableTouchSlopItemTouchHelper(new AdjustableTouchSlopItemTouchHelper.Callback() {
            boolean exceedThreshold = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof CommentsListingRecyclerViewAdapter.CommentBaseViewHolder)) {
                    return makeMovementFlags(0, 0);
                }
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                if (shouldSwipeBack) {
                    shouldSwipeBack = false;
                    return 0;
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int horizontalOffset = (int) Utils.convertDpToPixel(16, mActivity);
                if (dX > 0) {
                    if (dX > (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold) {
                        dX = (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold;
                        if (!exceedThreshold && isCurrentlyActive) {
                            exceedThreshold = true;
                            if (vibrateWhenActionTriggered) {
                                itemView.setHapticFeedbackEnabled(true);
                                itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                            }
                        }
                        backgroundSwipeRight.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else {
                        exceedThreshold = false;
                        backgroundSwipeRight.setBounds(0, 0, 0, 0);
                    }

                    drawableSwipeRight.setBounds(itemView.getLeft() + ((int) dX) - horizontalOffset - drawableSwipeRight.getIntrinsicWidth(),
                            (itemView.getBottom() + itemView.getTop() - drawableSwipeRight.getIntrinsicHeight()) / 2,
                            itemView.getLeft() + ((int) dX) - horizontalOffset,
                            (itemView.getBottom() + itemView.getTop() + drawableSwipeRight.getIntrinsicHeight()) / 2);
                    backgroundSwipeRight.draw(c);
                    drawableSwipeRight.draw(c);
                } else if (dX < 0) {
                    if (-dX > (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold) {
                        dX = -(itemView.getRight() - itemView.getLeft()) * swipeActionThreshold;
                        if (!exceedThreshold && isCurrentlyActive) {
                            exceedThreshold = true;
                            if (vibrateWhenActionTriggered) {
                                itemView.setHapticFeedbackEnabled(true);
                                itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                            }
                        }
                        backgroundSwipeLeft.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    } else {
                        exceedThreshold = false;
                        backgroundSwipeLeft.setBounds(0, 0, 0, 0);
                    }
                    drawableSwipeLeft.setBounds(itemView.getRight() + ((int) dX) + horizontalOffset,
                            (itemView.getBottom() + itemView.getTop() - drawableSwipeLeft.getIntrinsicHeight()) / 2,
                            itemView.getRight() + ((int) dX) + horizontalOffset + drawableSwipeLeft.getIntrinsicWidth(),
                            (itemView.getBottom() + itemView.getTop() + drawableSwipeLeft.getIntrinsicHeight()) / 2);
                    backgroundSwipeLeft.draw(c);
                    drawableSwipeLeft.draw(c);
                }

                if (!isCurrentlyActive && exceedThreshold) {
                    mAdapter.onItemSwipe(viewHolder, dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START, swipeLeftAction, swipeRightAction);
                    exceedThreshold = false;
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 100;
            }
        });

        binding.recyclerViewCommentsListingFragment.setOnTouchListener((view, motionEvent) -> {
            shouldSwipeBack = motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP;
            return false;
        });

        if (enableSwipeAction) {
            touchHelper.attachToRecyclerView(binding.recyclerViewCommentsListingFragment, 5);
        }

        new Handler().postDelayed(this::bindView, 0);

        return binding.getRoot();
    }

    private void bindView() {
        if (mActivity != null && !mActivity.isFinishing() && !mActivity.isDestroyed()) {
            mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
            binding.recyclerViewCommentsListingFragment.setLayoutManager(mLinearLayoutManager);

            String username = getArguments().getString(EXTRA_USERNAME);
            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_USER_COMMENT, SortType.Type.NEW.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_USER_COMMENT, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()), SortType.Time.valueOf(sortTime.toUpperCase()));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
            }

            mAdapter = new CommentsListingRecyclerViewAdapter(mActivity, mOauthRetrofit, customThemeWrapper,
                    getResources().getConfiguration().locale, mSharedPreferences,
                    mActivity.accessToken, mActivity.accountName,
                    username, () -> mCommentViewModel.retryLoadingMore());

            binding.recyclerViewCommentsListingFragment.setAdapter(mAdapter);

            if (mActivity instanceof RecyclerViewContentScrollingInterface) {
                binding.recyclerViewCommentsListingFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy > 0) {
                            ((RecyclerViewContentScrollingInterface) mActivity).contentScrollDown();
                        } else if (dy < 0) {
                            ((RecyclerViewContentScrollingInterface) mActivity).contentScrollUp();
                        }
                    }
                });
            }

            CommentViewModel.Factory factory;

            if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                factory = new CommentViewModel.Factory(mExecutor, mActivity.mHandler, mRetrofit,
                        null, mActivity.accountName, username, sortType,
                        getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
            } else {
                factory = new CommentViewModel.Factory(mExecutor, mActivity.mHandler, mOauthRetrofit,
                        mActivity.accessToken, mActivity.accountName, username, sortType,
                        getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
            }

            mCommentViewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
            mCommentViewModel.getComments().observe(getViewLifecycleOwner(), comments -> mAdapter.submitList(comments));

            mCommentViewModel.hasComment().observe(getViewLifecycleOwner(), hasComment -> {
                binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);
                if (hasComment) {
                    binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setVisibility(View.GONE);
                } else {
                    boolean areSavedComments = getArguments() != null && getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS);
                    String usernameArg = getArguments() != null ? getArguments().getString(EXTRA_USERNAME) : null;
                    if (!areSavedComments && usernameArg != null && !usernameArg.isEmpty()) {
                        showLoadCommentsFromArchiveView();
                    } else {
                        binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setOnClickListener(null);
                        binding.fetchCommentsInfoButtonCommentsListingFragment.setVisibility(View.GONE);
                        showErrorView(R.string.no_comments);
                    }
                }
            });

            mCommentViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
                if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                    binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);
                } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                    binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);
                    binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setOnClickListener(view -> refresh());
                    binding.fetchCommentsInfoButtonCommentsListingFragment.setVisibility(View.GONE);
                    showErrorView(R.string.load_comments_failed);
                } else {
                    binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(true);
                }
            });

            mCommentViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> mAdapter.setNetworkState(networkState));

            binding.swipeRefreshLayoutViewCommentsListingFragment.setOnRefreshListener(() -> mCommentViewModel.refresh());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.setCanStartActivity(true);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void changeSortType(SortType sortType) {
        mCommentViewModel.changeSortType(sortType);
        this.sortType = sortType;
    }

    private void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(customThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(customThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(customThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(customThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (BaseActivity) context;
    }

    @Override
    public void refresh() {
        binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setVisibility(View.GONE);
        mCommentViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayoutViewCommentsListingFragment.setProgressBackgroundColorSchemeColor(customThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutViewCommentsListingFragment.setColorSchemeColors(customThemeWrapper.getColorAccent());
        binding.fetchCommentsInfoTextViewCommentsListingFragment.setTextColor(customThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.fetchCommentsInfoTextViewCommentsListingFragment.setTypeface(mActivity.typeface);
        }
        binding.fetchCommentsInfoButtonCommentsListingFragment.setBackgroundColor(customThemeWrapper.getColorPrimaryLightTheme());
        binding.fetchCommentsInfoButtonCommentsListingFragment.setTextColor(customThemeWrapper.getButtonTextColor());
        if (mActivity.typeface != null) {
            binding.fetchCommentsInfoButtonCommentsListingFragment.setTypeface(mActivity.typeface);
        }
    }

    private void showLoadCommentsFromArchiveView() {
        showErrorView(R.string.no_comments);
        binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setOnClickListener(null);
        binding.fetchCommentsInfoButtonCommentsListingFragment.setVisibility(View.VISIBLE);
        binding.fetchCommentsInfoButtonCommentsListingFragment.setText(R.string.load_comments_from_archive);
        binding.fetchCommentsInfoButtonCommentsListingFragment.setBackgroundColor(customThemeWrapper.getColorPrimaryLightTheme());
        binding.fetchCommentsInfoButtonCommentsListingFragment.setTextColor(customThemeWrapper.getButtonTextColor());
        if (mActivity.typeface != null) {
            binding.fetchCommentsInfoButtonCommentsListingFragment.setTypeface(mActivity.typeface);
        }
        binding.fetchCommentsInfoButtonCommentsListingFragment.setOnClickListener(view -> loadCommentsFromArchive());
    }

    private void loadCommentsFromArchive() {
        binding.fetchCommentsInfoButtonCommentsListingFragment.setOnClickListener(null);
        binding.fetchCommentsInfoButtonCommentsListingFragment.setVisibility(View.GONE);
        binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(true);
        String usernameArg = getArguments() != null ? getArguments().getString(EXTRA_USERNAME) : null;
        if (usernameArg == null) {
            binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);
            return;
        }
        mExecutor.execute(() -> {
            ArcticShiftUserListingFetcher fetcher = new ArcticShiftUserListingFetcher();
            ArrayList<Comment> archivedComments = fetcher.fetchComments(usernameArg);
            String debugInfo = fetcher.getLastDebugInfo();
            String errorInfo = fetcher.getLastError();
            mActivity.mHandler.post(() -> {
                if (!isAdded() || mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) {
                    return;
                }

                binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);

                if (archivedComments.isEmpty()) {
                    // Show debug info inline only on error/empty
                    StringBuilder msg = new StringBuilder();
                    msg.append("Comments found: 0\n");
                    if (!errorInfo.isEmpty()) {
                        msg.append("ERROR: ").append(errorInfo).append("\n");
                    }
                    msg.append("\n").append(debugInfo);
                    binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setVisibility(View.VISIBLE);
                    binding.fetchCommentsInfoImageViewCommentsListingFragment.setVisibility(View.GONE);
                    binding.fetchCommentsInfoTextViewCommentsListingFragment.setText(msg.toString());
                    binding.fetchCommentsInfoTextViewCommentsListingFragment.setTextIsSelectable(true);
                    binding.fetchCommentsInfoButtonCommentsListingFragment.setOnClickListener(view -> loadCommentsFromArchive());
                    binding.fetchCommentsInfoButtonCommentsListingFragment.setVisibility(View.VISIBLE);
                    return;
                }

                // Success — hide info panel and show comments
                binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setVisibility(View.GONE);

                androidx.paging.PagedList.Config config = new androidx.paging.PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(archivedComments.size())
                        .build();

                androidx.paging.PagedList<Comment> pagedList = new androidx.paging.PagedList.Builder<>(
                        new androidx.paging.PositionalDataSource<Comment>() {
                            @Override
                            public void loadInitial(@NonNull androidx.paging.PositionalDataSource.LoadInitialParams params, @NonNull androidx.paging.PositionalDataSource.LoadInitialCallback<Comment> callback) {
                                callback.onResult(archivedComments, 0, archivedComments.size());
                            }

                            @Override
                            public void loadRange(@NonNull androidx.paging.PositionalDataSource.LoadRangeParams params, @NonNull androidx.paging.PositionalDataSource.LoadRangeCallback<Comment> callback) {
                            }
                        }, config)
                        .setNotifyExecutor(runnable -> mActivity.mHandler.post(runnable))
                        .setFetchExecutor(mExecutor)
                        .build();

                mAdapter.submitList(pagedList);
            });
        });
    }

    private void showErrorView(int stringResId) {
        if (mActivity != null && isAdded()) {
            binding.swipeRefreshLayoutViewCommentsListingFragment.setRefreshing(false);
            binding.fetchCommentsInfoLinearLayoutCommentsListingFragment.setVisibility(View.VISIBLE);
            binding.fetchCommentsInfoTextViewCommentsListingFragment.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(binding.fetchCommentsInfoImageViewCommentsListingFragment);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public SortType getSortType() {
        return sortType;
    }

    public void editComment(Comment comment, int position) {
        if (mAdapter != null) {
            mAdapter.editComment(comment, position);
        }
    }

    public void editComment(String commentMarkdown, int position) {
        if (mAdapter != null) {
            mAdapter.editComment(commentMarkdown, position);
        }
    }

    public void toggleReplyNotifications(Comment comment, int position) {
        ReplyNotificationsToggle.toggleEnableNotification(new Handler(Looper.getMainLooper()), mOauthRetrofit,
                mActivity.accessToken, comment, new ReplyNotificationsToggle.SendNotificationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(mActivity,
                                comment.isSendReplies() ? R.string.reply_notifications_disabled : R.string.reply_notifications_enabled,
                                Toast.LENGTH_SHORT).show();
                        mAdapter.toggleReplyNotifications(position);
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(mActivity, R.string.toggle_reply_notifications_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        if (mAdapter != null) {
            String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                mAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void refreshAdapter(RecyclerView recyclerView, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        int previousPosition = -1;
        if (recyclerView.getLayoutManager() != null) {
            previousPosition = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        if (previousPosition > 0) {
            recyclerView.scrollToPosition(previousPosition);
        }
    }
}
