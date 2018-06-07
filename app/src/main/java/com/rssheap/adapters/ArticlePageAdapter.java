package com.rssheap.adapters;

import java.util.List;

import com.rssheap.R;
import com.rssheap.fragments.ArticleFragment;
import com.rssheap.model.Article;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ArticlePageAdapter extends android.support.v4.app.FragmentPagerAdapter {
	private List<Article> articles;

    public ArticlePageAdapter(FragmentManager fm, List<Article> articles) {
        super(fm);
        this.articles = articles;
    }

    public List<Article> getArticles() {
    	return articles;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        ArticleFragment articleFragment = new ArticleFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("article", articles.get(position));
        articleFragment.setArguments(bundle);
        return articleFragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        /*WebView webView = (WebView) container.findViewById(R.id.webView);
        webView.stopLoading();
        webView.clearHistory();
        webView.clearCache(true);
        webView.loadUrl("about:blank");
        webView.freeMemory();
        webView.clearView();
        webView.pauseTimers();
        webView.destroyDrawingCache();
        webView.destroy();
        webView = null;*/
        super.destroyItem(container, position, object);
    }

    public void addRange(List<Article> articles) {
    	this.articles.addAll(articles);
    }

    @Override
    public int getCount() {
        return this.articles.size();
    }
}