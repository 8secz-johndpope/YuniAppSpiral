/*
 *  Copyright 2016 Piruin Panichphol
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.yuniapp.quickAction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

/**
 * Builder to create QuickAction with ActionItem of Intent Activity or Service
 */
public class QuickIntentAction {
  private static final int SERVICE = 1;
  private static final int ACTIVITY = 0;

  private Context mContext;
  private Intent mIntent;
  private int mOrientation;
  private int mIntentType = ACTIVITY;
  private QuickAction.OnActionItemClickListener mOnActionItemClick;
  private String mType[] = {"Activity", "Service"};

  /**
   * Constructor for default vertical layout
   *
   * @param context require
   */
  public QuickIntentAction(Context context) {
    this(context, QuickAction.VERTICAL);
  }

  public QuickIntentAction(Context context, int orientation) {
    mContext = context;
    mOrientation = orientation;
  }

  public QuickIntentAction setServiceIntent(Intent services) {
    mIntent = services;
    mIntentType = SERVICE;
    return this;
  }

  public QuickIntentAction setActivityIntent(Intent activity) {
    mIntent = activity;
    mIntentType = ACTIVITY;
    return this;
  }

  public QuickIntentAction serOnActionItemClickListener(QuickAction.OnActionItemClickListener onClick) {
    mOnActionItemClick = onClick;
    return this;
  }

  public QuickAction create() {
    if (mIntent == null)
      throw new IllegalStateException(
        "Must set intent be for create(), Use setActivityIntent() or "+"setServiceIntent()");

    QuickAction quickAction = new QuickAction(mContext, mOrientation);
    // Add List of Support Activity or Services
    if (mIntent != null) {
      final List<ResolveInfo> lists;
      PackageManager pm = mContext.getPackageManager();

      switch (mIntentType) {
        case SERVICE:
          lists = pm.queryIntentServices(mIntent, 0);
          break;
        case ACTIVITY:
        default:
          lists = pm.queryIntentActivities(mIntent, 0);
          break;
      }
      // Add Action Item of support intent.
      if (lists.size() > 0) {
        int index = 0;
        for (ResolveInfo info : lists) {
          ActionItem item = new ActionItem(index++, (String)info.loadLabel(pm));
          item.setIconDrawable(info.loadIcon(pm));
          quickAction.addActionItem(item);
        }
        addOnActionItemClick(quickAction, lists);
      } else {
        ActionItem item = new ActionItem(0, "Not found support any"+mType[mIntentType]+"!");
        quickAction.addActionItem(item);
      }
    }
    return quickAction;
  }

  private void addOnActionItemClick(QuickAction action, final List<ResolveInfo> lists) {
    // If not explicit add then we'll Add Default OnActionItemClick
    if (mOnActionItemClick != null)
      action.setOnActionItemClickListener(mOnActionItemClick);
    else {
      setDefaultOnActionItemClick(action, lists);
    }
  }

  private void setDefaultOnActionItemClick(QuickAction action, final List<ResolveInfo> lists) {
    switch (mIntentType) {
      case SERVICE:
        action.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
          @Override public void onItemClick(ActionItem item) {
            ResolveInfo info = lists.get(item.getActionId());
            String name = info.serviceInfo.name;
            String packageName = info.serviceInfo.packageName;

            Intent service = new Intent(mIntent);
            service.setComponent(new ComponentName(packageName, name));
            mContext.startService(service);
          }
        });
        break;
      case ACTIVITY:
      default:
        action.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
          @Override public void onItemClick(ActionItem item) {
            ResolveInfo info = lists.get(item.getActionId());
            String name = info.activityInfo.name;
            String packageName = info.activityInfo.packageName;

            Intent intent = new Intent(mIntent);
            intent.setComponent(new ComponentName(packageName, name));
            mContext.startActivity(intent);
          }
        });
        break;
    }
  }
}
