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
    view = finder.findRequiredView(source, 2131492979, "method 'onBLEButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onBLEButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492981, "method 'onUARTButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onUARTButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492983, "method 'onQuaternionButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onQuaternionButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492985, "method 'onMAGButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onMAGButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492987, "method 'onLOCKButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onLOCKButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492989, "method 'onERASEButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onERASEButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492991, "method 'onRECORDButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onRECORDButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492993, "method 'onPLAYBACKButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onPLAYBACKButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492995, "method 'onLED0ButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onLED0ButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131492997, "method 'onLED1ButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onLED1ButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131493001, "method 'onEEPROMButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onEEPROMButtonClick(p0);
        }
      });
    view = finder.findRequiredView(source, 2131493003, "method 'onCHARGEButtonClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onCHARGEButtonClick(p0);
        }
      });
  }

  @Override public void reset(T target) {
  }
}
