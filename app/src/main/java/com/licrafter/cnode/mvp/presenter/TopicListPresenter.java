package com.licrafter.cnode.mvp.presenter;

import com.licrafter.cnode.api.CNodeApi;
import com.licrafter.cnode.model.TabModel;
import com.licrafter.cnode.ui.fragment.TopicListFragment;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author: shell
 * date 2017/2/24 下午4:28
 **/
public class TopicListPresenter extends BasePresenter<TopicListFragment> {

    private static final int PAGE_LIMIT = 50;
    private int mPageIndex = 1;
    private String mTab;
    private boolean mHasNextPage = true;
    private boolean mLoading = false;


    public void refresh() {
        mPageIndex = 1;
        getHomePage(mPageIndex, true);
    }

    public void loadNextPage() {
        getHomePage(mPageIndex, false);
    }


    private void getHomePage(final int pageIndex, final boolean refresh) {
        if (mLoading) {
            return;
        }
        mLoading = true;
        mCompositeSubscription.add(getObservable(pageIndex).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TabModel>() {
                    @Override
                    public void onCompleted() {
                        mLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().onFailed(e);
                    }

                    @Override
                    public void onNext(TabModel tabModel) {
                        mPageIndex++;
                        mHasNextPage = tabModel.getData().size() >= PAGE_LIMIT;

                        if (tabModel.isSuccess()) {
                            getView().notifySuccess(tabModel, refresh);
                        } else {
                            getView().onFailed(new Throwable("获取列表失败"));
                        }
                    }
                }));

    }

    private Observable<TabModel> getObservable(int pageIndex) {
        if (mTab == null) {
            return CNodeApi.getCNodeService().getTopicPage(pageIndex, PAGE_LIMIT, true);
        } else {
            return CNodeApi.getCNodeService().getTabByName(mTab, pageIndex, PAGE_LIMIT, true);
        }
    }

    public void setTab(String tab) {
        mTab = tab;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public boolean hasNextPage() {
        return mHasNextPage;
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex) {
        mPageIndex = pageIndex;
    }
}
