// Generated code from Butter Knife. Do not modify!
package com.badlogic.invaders.android;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class BLEDeviceScanActivity$$ViewInjector<T extends com.badlogic.invaders.android.BLEDeviceScanActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492978, "method 'onRefreshButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onRefreshButtonClick(p0);
        }
      });
  }

  @Override public void reset(T target) {
  }
}
