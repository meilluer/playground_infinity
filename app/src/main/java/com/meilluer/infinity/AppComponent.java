package com.meilluer.infinity;

import android.app.Application;
import android.os.Handler;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import com.meilluer.infinity.activities.AccountPostsActivity;
import com.meilluer.infinity.activities.AccountSavedThingActivity;
import com.meilluer.infinity.activities.CommentActivity;
import com.meilluer.infinity.activities.CommentFilterPreferenceActivity;
import com.meilluer.infinity.activities.CommentFilterUsageListingActivity;
import com.meilluer.infinity.activities.CreateMultiRedditActivity;
import com.meilluer.infinity.activities.CustomThemeListingActivity;
import com.meilluer.infinity.activities.CustomThemePreviewActivity;
import com.meilluer.infinity.activities.CustomizeCommentFilterActivity;
import com.meilluer.infinity.activities.CustomizePostFilterActivity;
import com.meilluer.infinity.activities.CustomizeThemeActivity;
import com.meilluer.infinity.activities.EditCommentActivity;
import com.meilluer.infinity.activities.EditMultiRedditActivity;
import com.meilluer.infinity.activities.EditPostActivity;
import com.meilluer.infinity.activities.EditProfileActivity;
import com.meilluer.infinity.activities.FetchRandomSubredditOrPostActivity;
import com.meilluer.infinity.activities.FilteredPostsActivity;
import com.meilluer.infinity.activities.FullMarkdownActivity;
import com.meilluer.infinity.activities.HistoryActivity;
import com.meilluer.infinity.activities.InboxActivity;
import com.meilluer.infinity.activities.LinkResolverActivity;
import com.meilluer.infinity.activities.LockScreenActivity;
import com.meilluer.infinity.activities.LoginActivity;
import com.meilluer.infinity.activities.LoginChromeCustomTabActivity;
import com.meilluer.infinity.activities.MainActivity;
import com.meilluer.infinity.activities.PostFilterPreferenceActivity;
import com.meilluer.infinity.activities.PostFilterUsageListingActivity;
import com.meilluer.infinity.activities.PostGalleryActivity;
import com.meilluer.infinity.activities.PostImageActivity;
import com.meilluer.infinity.activities.PostLinkActivity;
import com.meilluer.infinity.activities.PostPollActivity;
import com.meilluer.infinity.activities.PostTextActivity;
import com.meilluer.infinity.activities.PostVideoActivity;
import com.meilluer.infinity.activities.ReportActivity;
import com.meilluer.infinity.activities.RulesActivity;
import com.meilluer.infinity.activities.SavedSubredditsActivity;
import com.meilluer.infinity.activities.SearchActivity;
import com.meilluer.infinity.activities.SearchResultActivity;
import com.meilluer.infinity.activities.SearchSubredditsResultActivity;
import com.meilluer.infinity.activities.SearchUsersResultActivity;
import com.meilluer.infinity.activities.SelectUserFlairActivity;
import com.meilluer.infinity.activities.SelectedSubredditsAndUsersActivity;
import com.meilluer.infinity.activities.SendPrivateMessageActivity;
import com.meilluer.infinity.activities.SettingsActivity;
import com.meilluer.infinity.activities.SubmitCrosspostActivity;
import com.meilluer.infinity.activities.SubredditMultiselectionActivity;
import com.meilluer.infinity.activities.SubscribedThingListingActivity;
import com.meilluer.infinity.activities.SuicidePreventionActivity;
import com.meilluer.infinity.activities.ViewImageOrGifActivity;
import com.meilluer.infinity.activities.ViewImgurMediaActivity;
import com.meilluer.infinity.activities.ViewMultiRedditDetailActivity;
import com.meilluer.infinity.activities.ViewPostDetailActivity;
import com.meilluer.infinity.activities.ViewPrivateMessagesActivity;
import com.meilluer.infinity.activities.ViewRedditGalleryActivity;
import com.meilluer.infinity.activities.ViewSubredditDetailActivity;
import com.meilluer.infinity.activities.ViewUserDetailActivity;
import com.meilluer.infinity.activities.ViewVideoActivity;
import com.meilluer.infinity.activities.WebViewActivity;
import com.meilluer.infinity.activities.WikiActivity;
import com.meilluer.infinity.bottomsheetfragments.AccountChooserBottomSheetFragment;
import com.meilluer.infinity.bottomsheetfragments.CommentMoreBottomSheetFragment;
import com.meilluer.infinity.bottomsheetfragments.FlairBottomSheetFragment;
import com.meilluer.infinity.bottomsheetfragments.PostOptionsBottomSheetFragment;
import com.meilluer.infinity.fragments.CommentsListingFragment;
import com.meilluer.infinity.fragments.CustomThemeListingFragment;
import com.meilluer.infinity.fragments.FollowedUsersListingFragment;
import com.meilluer.infinity.fragments.HistoryPostFragment;
import com.meilluer.infinity.fragments.InboxFragment;
import com.meilluer.infinity.fragments.MorePostsInfoFragment;
import com.meilluer.infinity.fragments.MultiRedditListingFragment;
import com.meilluer.infinity.fragments.OfflineFragment;
import com.meilluer.infinity.fragments.OfflinePostFragment;
import com.meilluer.infinity.activities.OfflinePostsActivity;
import com.meilluer.infinity.fragments.PostFragment;
import com.meilluer.infinity.fragments.SidebarFragment;
import com.meilluer.infinity.fragments.SubredditListingFragment;
import com.meilluer.infinity.fragments.SubscribedSubredditsListingFragment;
import com.meilluer.infinity.fragments.UserListingFragment;
import com.meilluer.infinity.fragments.ViewImgurImageFragment;
import com.meilluer.infinity.fragments.ViewImgurVideoFragment;
import com.meilluer.infinity.fragments.ViewPostDetailFragment;
import com.meilluer.infinity.fragments.ViewRedditGalleryImageOrGifFragment;
import com.meilluer.infinity.fragments.ViewRedditGalleryVideoFragment;
import com.meilluer.infinity.services.DownloadMediaService;
import com.meilluer.infinity.services.DownloadRedditVideoService;
import com.meilluer.infinity.services.EditProfileService;
import com.meilluer.infinity.services.SubmitPostService;
import com.meilluer.infinity.settings.AdvancedPreferenceFragment;
import com.meilluer.infinity.settings.CommentPreferenceFragment;
import com.meilluer.infinity.settings.CrashReportsFragment;
import com.meilluer.infinity.settings.CustomizeBottomAppBarFragment;
import com.meilluer.infinity.settings.CustomizeMainPageTabsFragment;
import com.meilluer.infinity.settings.DownloadLocationPreferenceFragment;
import com.meilluer.infinity.settings.FontPreferenceFragment;
import com.meilluer.infinity.settings.GesturesAndButtonsPreferenceFragment;
import com.meilluer.infinity.settings.MainPreferenceFragment;
import com.meilluer.infinity.settings.MiscellaneousPreferenceFragment;
import com.meilluer.infinity.settings.NotificationPreferenceFragment;
import com.meilluer.infinity.settings.NsfwAndSpoilerFragment;
import com.meilluer.infinity.settings.PostHistoryFragment;
import com.meilluer.infinity.settings.ProxyPreferenceFragment;
import com.meilluer.infinity.settings.SecurityPreferenceFragment;
import com.meilluer.infinity.settings.ThemePreferenceFragment;
import com.meilluer.infinity.settings.TranslationFragment;
import com.meilluer.infinity.settings.VideoPreferenceFragment;
import com.meilluer.infinity.worker.MaterialYouWorker;
import com.meilluer.infinity.worker.PullNotificationWorker;
import com.meilluer.infinity.liveactivity.LiveActivityWorker;
import com.meilluer.infinity.liveactivity.LiveActivityReceiver;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(LoginActivity loginActivity);

    void inject(PostFragment postFragment);

    void inject(OfflinePostFragment offlinePostFragment);
    
    void inject(OfflinePostsActivity offlinePostsActivity);

    void inject(OfflineFragment offlineFragment);

    void inject(SubredditListingFragment subredditListingFragment);

    void inject(UserListingFragment userListingFragment);

    void inject(ViewPostDetailActivity viewPostDetailActivity);

    void inject(ViewSubredditDetailActivity viewSubredditDetailActivity);

    void inject(ViewUserDetailActivity viewUserDetailActivity);

    void inject(CommentActivity commentActivity);

    void inject(SubscribedThingListingActivity subscribedThingListingActivity);

    void inject(PostTextActivity postTextActivity);

    void inject(SubscribedSubredditsListingFragment subscribedSubredditsListingFragment);

    void inject(PostLinkActivity postLinkActivity);

    void inject(PostImageActivity postImageActivity);

    void inject(PostVideoActivity postVideoActivity);

    void inject(FlairBottomSheetFragment flairBottomSheetFragment);

    void inject(RulesActivity rulesActivity);

    void inject(CommentsListingFragment commentsListingFragment);

    void inject(SubmitPostService submitPostService);

    void inject(FilteredPostsActivity filteredPostsActivity);

    void inject(SearchResultActivity searchResultActivity);

    void inject(SearchSubredditsResultActivity searchSubredditsResultActivity);

    void inject(FollowedUsersListingFragment followedUsersListingFragment);

    void inject(EditPostActivity editPostActivity);

    void inject(EditCommentActivity editCommentActivity);

    void inject(AccountPostsActivity accountPostsActivity);

    void inject(PullNotificationWorker pullNotificationWorker);

    void inject(LiveActivityWorker liveActivityWorker);

    void inject(LiveActivityReceiver liveActivityReceiver);

    void inject(InboxActivity inboxActivity);

    void inject(NotificationPreferenceFragment notificationPreferenceFragment);

    void inject(LinkResolverActivity linkResolverActivity);

    void inject(SearchActivity searchActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(MainPreferenceFragment mainPreferenceFragment);

    void inject(AccountSavedThingActivity accountSavedThingActivity);

    void inject(ViewImageOrGifActivity viewGIFActivity);

    void inject(ViewMultiRedditDetailActivity viewMultiRedditDetailActivity);

    void inject(ViewVideoActivity viewVideoActivity);

    void inject(GesturesAndButtonsPreferenceFragment gesturesAndButtonsPreferenceFragment);

    void inject(CreateMultiRedditActivity createMultiRedditActivity);

    void inject(SubredditMultiselectionActivity subredditMultiselectionActivity);

    void inject(ThemePreferenceFragment themePreferenceFragment);

    void inject(CustomizeThemeActivity customizeThemeActivity);

    void inject(CustomThemeListingActivity customThemeListingActivity);

    void inject(SidebarFragment sidebarFragment);

    void inject(AdvancedPreferenceFragment advancedPreferenceFragment);

    void inject(CustomThemePreviewActivity customThemePreviewActivity);

    void inject(EditMultiRedditActivity editMultiRedditActivity);

    void inject(SelectedSubredditsAndUsersActivity selectedSubredditsAndUsersActivity);

    void inject(ReportActivity reportActivity);

    void inject(ViewImgurMediaActivity viewImgurMediaActivity);

    void inject(ViewImgurVideoFragment viewImgurVideoFragment);

    void inject(DownloadRedditVideoService downloadRedditVideoService);

    void inject(MultiRedditListingFragment multiRedditListingFragment);

    void inject(InboxFragment inboxFragment);

    void inject(ViewPrivateMessagesActivity viewPrivateMessagesActivity);

    void inject(SendPrivateMessageActivity sendPrivateMessageActivity);

    void inject(VideoPreferenceFragment videoPreferenceFragment);

    void inject(ViewRedditGalleryActivity viewRedditGalleryActivity);

    void inject(ViewRedditGalleryVideoFragment viewRedditGalleryVideoFragment);

    void inject(CustomizeMainPageTabsFragment customizeMainPageTabsFragment);

    void inject(DownloadMediaService downloadMediaService);

    void inject(DownloadLocationPreferenceFragment downloadLocationPreferenceFragment);

    void inject(SubmitCrosspostActivity submitCrosspostActivity);

    void inject(FullMarkdownActivity fullMarkdownActivity);

    void inject(SelectUserFlairActivity selectUserFlairActivity);

    void inject(SecurityPreferenceFragment securityPreferenceFragment);

    void inject(NsfwAndSpoilerFragment nsfwAndSpoilerFragment);

    void inject(CustomizeBottomAppBarFragment customizeBottomAppBarFragment);

    void inject(TranslationFragment translationFragment);

    void inject(FetchRandomSubredditOrPostActivity fetchRandomSubredditOrPostActivity);

    void inject(MiscellaneousPreferenceFragment miscellaneousPreferenceFragment);

    void inject(CustomizePostFilterActivity customizePostFilterActivity);

    void inject(PostHistoryFragment postHistoryFragment);

    void inject(PostFilterPreferenceActivity postFilterPreferenceActivity);

    void inject(PostFilterUsageListingActivity postFilterUsageListingActivity);

    void inject(SearchUsersResultActivity searchUsersResultActivity);

    void inject(ViewImgurImageFragment viewImgurImageFragment);

    void inject(ViewRedditGalleryImageOrGifFragment viewRedditGalleryImageOrGifFragment);

    void inject(ViewPostDetailFragment viewPostDetailFragment);

    void inject(SuicidePreventionActivity suicidePreventionActivity);

    void inject(WebViewActivity webViewActivity);

    void inject(CrashReportsFragment crashReportsFragment);

    void inject(LockScreenActivity lockScreenActivity);

    void inject(PostGalleryActivity postGalleryActivity);

    void inject(WikiActivity wikiActivity);

    void inject(Infinity infinity);

    void inject(EditProfileService editProfileService);

    void inject(EditProfileActivity editProfileActivity);

    void inject(FontPreferenceFragment fontPreferenceFragment);

    void inject(CommentPreferenceFragment commentPreferenceFragment);

    void inject(PostPollActivity postPollActivity);

    void inject(AccountChooserBottomSheetFragment accountChooserBottomSheetFragment);

    void inject(MaterialYouWorker materialYouWorker);

    void inject(HistoryPostFragment historyPostFragment);

    void inject(HistoryActivity historyActivity);

    void inject(MorePostsInfoFragment morePostsInfoFragment);

    void inject(CommentFilterPreferenceActivity commentFilterPreferenceActivity);

    void inject(CustomizeCommentFilterActivity customizeCommentFilterActivity);

    void inject(CommentFilterUsageListingActivity commentFilterUsageListingActivity);

    void inject(CustomThemeListingFragment customThemeListingFragment);

    void inject(LoginChromeCustomTabActivity loginChromeCustomTabActivity);

    void inject(PostOptionsBottomSheetFragment postOptionsBottomSheetFragment);

    void inject(CommentMoreBottomSheetFragment commentMoreBottomSheetFragment);

    void inject(ProxyPreferenceFragment proxyPreferenceFragment);

    void inject(SavedSubredditsActivity savedSubredditsActivity);

    @Component.Factory
    interface Factory {
        AppComponent create(@BindsInstance Application application, @BindsInstance Handler handler);
    }
}
