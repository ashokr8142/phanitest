/*
 * Copyright © 2017-2019 Harvard Pilgrim Health Care Institute (HPHCI) and its Contributors.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * Funding Source: Food and Drug Administration (“Funding Agency”) effective 18 September 2014 as Contract no. HHSF22320140030I/HHSF22301006T (the “Prime Contract”).
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.harvard.studyappmodule;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.harvard.R;
import com.harvard.studyappmodule.studymodel.Categories;
import com.harvard.studyappmodule.studymodel.Filter;
import com.harvard.studyappmodule.studymodel.ParticipationStatus;
import com.harvard.studyappmodule.studymodel.StudyStatus;
import com.harvard.utils.AppController;
import com.harvard.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterActivity extends AppCompatActivity {

  private AppCompatTextView mCancelTextView;
  private AppCompatTextView mApplyTextView;
  private AppCompatTextView mAllStudiesLabel;
  private AppCompatTextView mActiveLabel;
  private AppCompatTextView mClosedLabel;
  private AppCompatTextView mUpcomingLabel;
  private AppCompatTextView mParticipationStatusLabel;
  private RelativeLayout mParticipationStatusLayout;
  private AppCompatTextView mInProgressLabel;
  private AppCompatTextView mYettoJoinLabel;
  private AppCompatTextView mBookmarkedLabel;
  private AppCompatTextView mCompletedLabel;
  private AppCompatTextView mWithdrawnLabel;
  private AppCompatTextView mNotEligibleLabel;
  private AppCompatTextView mCategoriesLabel;
  private AppCompatTextView mCategory1Label;
  private AppCompatTextView mCategory2Label;
  private AppCompatTextView mCategory3Label;
  private AppCompatTextView mCategory4Label;
  private AppCompatTextView mCategory5Label;
  private AppCompatTextView mCategory6Label;
  private AppCompatTextView mCategory7Label;
  private AppCompatTextView mCategory8Label;
  private AppCompatTextView mCategory9Label;
  private AppCompatTextView mCategory10Label;
  private AppCompatCheckBox mActiveSelectBtn;
  private AppCompatCheckBox mClosedSelectBtn;
  private AppCompatCheckBox mUpcomingSelectBtn;
  private AppCompatCheckBox mInProgressSelctBtn;
  private AppCompatCheckBox mYettoJoinSelctBtn;
  private AppCompatCheckBox mBookmarkedSelctBtn;
  private AppCompatCheckBox mCompletedSelectBtn;
  private AppCompatCheckBox mWithdrawnSelectBtn;
  private AppCompatCheckBox mNotEligibleSelectBtn;
  private AppCompatCheckBox mCategory1SelectBtn;
  private AppCompatCheckBox mCategory2SelectBtn;
  private AppCompatCheckBox mCategory3SelectBtn;
  private AppCompatCheckBox mCategory4SelectBtn;
  private AppCompatCheckBox mCategory5SelectBtn;
  private AppCompatCheckBox mCategory6SelectBtn;
  private AppCompatCheckBox mCategory7SelectBtn;
  private AppCompatCheckBox mCategory8SelectBtn;
  private AppCompatCheckBox mCategory9SelectBtn;
  private AppCompatCheckBox mCategory10SelectBtn;

  private RelativeLayout mInProgressLayout;
  private RelativeLayout mYettoJoinLayout;
  private RelativeLayout mCompletedLayout;
  private RelativeLayout mWithdrawnLayout;
  private RelativeLayout mNotEligibleLayout;
  private RelativeLayout mBookmarkedLayout;

  private AppCompatCheckBox mPausedSelectBtn;
  private AppCompatTextView mPausedLabel;
  private String mUserId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filter);
    initializeXMLId();
    mUserId =
        AppController.getHelperSharedPreference()
            .readPreference(this, getResources().getString(R.string.userid), "");
    if (mUserId.equalsIgnoreCase("")) {
      disableParticipationStatusBtn();
    } else {
      enableParticipationStatusBtn();
    }
    String jsonObjectString =
        AppController.getHelperSharedPreference()
            .readPreference(FilterActivity.this, getString(R.string.json_object_filter), "");
    if (jsonObjectString.equalsIgnoreCase("")) {
      defaultSelectedFilterOption();
    } else {
      setFilterSelected(jsonObjectString);
    }

    setBackgroundColorView();
    setFont();
    bindEvents();
  }

  private void initializeXMLId() {
    mAllStudiesLabel = (AppCompatTextView) findViewById(R.id.mAllStudiesLabel);
    mActiveLabel = (AppCompatTextView) findViewById(R.id.mActiveLabel);
    mPausedLabel = (AppCompatTextView) findViewById(R.id.mPausedLabel);
    mClosedLabel = (AppCompatTextView) findViewById(R.id.mClosedLabel);
    mUpcomingLabel = (AppCompatTextView) findViewById(R.id.mUpcomingLabel);
    mParticipationStatusLabel = (AppCompatTextView) findViewById(R.id.mParticipationStatusLabel);
    mParticipationStatusLayout = findViewById(R.id.mParticipationStatusLayout);
    mInProgressLabel = (AppCompatTextView) findViewById(R.id.mInProgressLabel);
    mYettoJoinLabel = (AppCompatTextView) findViewById(R.id.mYettoJoinLabel);
    mBookmarkedLabel = (AppCompatTextView) findViewById(R.id.mBookmarkedLabel);
    mCompletedLabel = (AppCompatTextView) findViewById(R.id.mCompletedLabel);
    mWithdrawnLabel = (AppCompatTextView) findViewById(R.id.mWithdrawnLabel);
    mNotEligibleLabel = (AppCompatTextView) findViewById(R.id.mNotEligibleLabel);
    mCategoriesLabel = (AppCompatTextView) findViewById(R.id.mCategoriesLabel);
    mCategory1Label = (AppCompatTextView) findViewById(R.id.mCategory1Label);
    mCategory2Label = (AppCompatTextView) findViewById(R.id.mCategory2Label);
    mCategory3Label = (AppCompatTextView) findViewById(R.id.mCategory3Label);
    mCategory4Label = (AppCompatTextView) findViewById(R.id.mCategory4Label);
    mCategory5Label = (AppCompatTextView) findViewById(R.id.mCategory5Label);
    mCategory6Label = (AppCompatTextView) findViewById(R.id.mCategory6Label);
    mCategory7Label = (AppCompatTextView) findViewById(R.id.mCategory7Label);
    mCategory8Label = (AppCompatTextView) findViewById(R.id.mCategory8Label);
    mCategory9Label = (AppCompatTextView) findViewById(R.id.mCategory9Label);
    mCategory10Label = (AppCompatTextView) findViewById(R.id.mCategory10Label);
    mCancelTextView = (AppCompatTextView) findViewById(R.id.mCancelTextView);
    mApplyTextView = (AppCompatTextView) findViewById(R.id.mApplyTextView);

    mActiveSelectBtn = (AppCompatCheckBox) findViewById(R.id.mActiveSelectBtn);
    mPausedSelectBtn = (AppCompatCheckBox) findViewById(R.id.mPausedSelectBtn);
    mClosedSelectBtn = (AppCompatCheckBox) findViewById(R.id.mClosedSelectBtn);
    mUpcomingSelectBtn = (AppCompatCheckBox) findViewById(R.id.mUpcomingSelectBtn);
    mInProgressSelctBtn = (AppCompatCheckBox) findViewById(R.id.mInProgressSelctBtn);
    mYettoJoinSelctBtn = (AppCompatCheckBox) findViewById(R.id.mYettoJoinSelctBtn);
    mBookmarkedSelctBtn = (AppCompatCheckBox) findViewById(R.id.mBookmarkedSelctBtn);
    mCompletedSelectBtn = (AppCompatCheckBox) findViewById(R.id.mCompletedSelectBtn);
    mWithdrawnSelectBtn = (AppCompatCheckBox) findViewById(R.id.mWithdrawnSelectBtn);
    mNotEligibleSelectBtn = (AppCompatCheckBox) findViewById(R.id.mNotEligibleSelectBtn);
    mCategory1SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory1SelectBtn);
    mCategory2SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory2SelectBtn);
    mCategory3SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory3SelectBtn);
    mCategory4SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory4SelectBtn);
    mCategory5SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory5SelectBtn);
    mCategory6SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory6SelectBtn);
    mCategory7SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory7SelectBtn);
    mCategory8SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory8SelectBtn);
    mCategory9SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory9SelectBtn);
    mCategory10SelectBtn = (AppCompatCheckBox) findViewById(R.id.mCategory10SelectBtn);

    mInProgressLayout = (RelativeLayout) findViewById(R.id.mInProgressLayout);
    mYettoJoinLayout = (RelativeLayout) findViewById(R.id.mYettoJoinLayout);
    mCompletedLayout = (RelativeLayout) findViewById(R.id.mCompletedLayout);
    mWithdrawnLayout = (RelativeLayout) findViewById(R.id.mWithdrawnLayout);
    mNotEligibleLayout = (RelativeLayout) findViewById(R.id.mNotEligibleLayout);
    mBookmarkedLayout = (RelativeLayout) findViewById(R.id.mBookmarkedLayout);
  }

  private void disableParticipationStatusBtn() {
    mParticipationStatusLabel.setVisibility(View.GONE);
    mParticipationStatusLayout.setVisibility(View.GONE);
    mInProgressLayout.setVisibility(View.GONE);
    mYettoJoinLayout.setVisibility(View.GONE);
    mCompletedLayout.setVisibility(View.GONE);
    mWithdrawnLayout.setVisibility(View.GONE);
    mNotEligibleLayout.setVisibility(View.GONE);
    mBookmarkedLayout.setVisibility(View.GONE);
  }

  private void enableParticipationStatusBtn() {
    mParticipationStatusLabel.setVisibility(View.VISIBLE);
    mParticipationStatusLayout.setVisibility(View.VISIBLE);
    mInProgressLayout.setVisibility(View.VISIBLE);
    mYettoJoinLayout.setVisibility(View.VISIBLE);
    mCompletedLayout.setVisibility(View.VISIBLE);
    mWithdrawnLayout.setVisibility(View.VISIBLE);
    mNotEligibleLayout.setVisibility(View.VISIBLE);
    mBookmarkedLayout.setVisibility(View.VISIBLE);
  }

  // default filter criteria; if any changes here then accordingly make changes in
  // StudyFragment.defaultSelectedFilterOption()
  private void defaultSelectedFilterOption() {
    mActiveSelectBtn.setChecked(true);
    mUpcomingSelectBtn.setChecked(true);
    mInProgressSelctBtn.setChecked(true);
    mYettoJoinSelctBtn.setChecked(true);
    mBookmarkedSelctBtn.setChecked(false);
    mCategory1SelectBtn.setChecked(true);
    mCategory2SelectBtn.setChecked(true);
    mCategory3SelectBtn.setChecked(true);
    mCategory4SelectBtn.setChecked(true);
    mCategory5SelectBtn.setChecked(true);
    mCategory6SelectBtn.setChecked(true);
    mCategory7SelectBtn.setChecked(true);
    mCategory8SelectBtn.setChecked(true);
    mCategory9SelectBtn.setChecked(true);
    mCategory10SelectBtn.setChecked(true);
  }

  private void setBackgroundColorView() {
    GradientDrawable bgShape1 = (GradientDrawable) mCancelTextView.getBackground();
    bgShape1.setColor(getResources().getColor(R.color.tab_color));

    GradientDrawable bgShape2 = (GradientDrawable) mApplyTextView.getBackground();
    bgShape2.setColor(getResources().getColor(R.color.tab_color));
  }

  private void setFont() {
    try {
      mAllStudiesLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "medium"));
      mActiveLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mClosedLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mUpcomingLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mParticipationStatusLabel.setTypeface(
          AppController.getTypeface(FilterActivity.this, "medium"));
      mInProgressLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mYettoJoinLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mBookmarkedLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCompletedLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mWithdrawnLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mNotEligibleLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategoriesLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "medium"));
      mCategory1Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory2Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory3Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory4Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory5Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory6Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory7Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory8Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory9Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
      mCategory10Label.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));

      mCancelTextView.setTypeface(AppController.getTypeface(FilterActivity.this, "medium"));
      mApplyTextView.setTypeface(AppController.getTypeface(FilterActivity.this, "medium"));
      mPausedLabel.setTypeface(AppController.getTypeface(FilterActivity.this, "regular"));
    } catch (Exception e) {
      Logger.log(e);
    }
  }

  private void bindEvents() {
    mCancelTextView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            finish();
          }
        });
    mApplyTextView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            Filter filter = new Filter();
            StudyStatus studyStatus = new StudyStatus();
            ParticipationStatus participationStatus = new ParticipationStatus();
            Categories categories = new Categories();

            if (mActiveSelectBtn.isChecked()) {
              studyStatus.setActive(true);
            } else {
              studyStatus.setActive(false);
            }
            if (mPausedSelectBtn.isChecked()) {
              studyStatus.setPaused(true);
            } else {
              studyStatus.setPaused(false);
            }
            if (mUpcomingSelectBtn.isChecked()) {
              studyStatus.setUpcoming(true);
            } else {
              studyStatus.setUpcoming(false);
            }
            if (mClosedSelectBtn.isChecked()) {
              studyStatus.setClosed(true);
            } else {
              studyStatus.setClosed(false);
            }
            if (mInProgressSelctBtn.isChecked()) {
              participationStatus.setInProgress(true);
            } else {
              participationStatus.setInProgress(false);
            }

            if (mYettoJoinSelctBtn.isChecked()) {
              participationStatus.setYetToJoin(true);
            } else {
              participationStatus.setYetToJoin(false);
            }

            if (mCompletedSelectBtn.isChecked()) {
              participationStatus.setCompleted(true);
            } else {
              participationStatus.setCompleted(false);
            }

            if (mWithdrawnSelectBtn.isChecked()) {
              participationStatus.setWithdrawn(true);
            } else {
              participationStatus.setWithdrawn(false);
            }
            if (mNotEligibleSelectBtn.isChecked()) {
              participationStatus.setNotEligible(true);
            } else {
              participationStatus.setNotEligible(false);
            }
            if (mBookmarkedSelctBtn.isChecked()) {
              filter.setBookmarked(true);
            } else {
              filter.setBookmarked(false);
            }
            if (mCategory1SelectBtn.isChecked()) {
              categories.setBiologicsSafety(true);
            } else {
              categories.setBiologicsSafety(false);
            }
            if (mCategory2SelectBtn.isChecked()) {
              categories.setClinicalTrials(true);
            } else {
              categories.setClinicalTrials(false);
            }
            if (mCategory3SelectBtn.isChecked()) {
              categories.setCosmeticsSafety(true);
            } else {
              categories.setCosmeticsSafety(false);
            }
            if (mCategory4SelectBtn.isChecked()) {
              categories.setDrugSafety(true);
            } else {
              categories.setDrugSafety(false);
            }
            if (mCategory5SelectBtn.isChecked()) {
              categories.setFoodSafety(true);
            } else {
              categories.setFoodSafety(false);
            }
            if (mCategory6SelectBtn.isChecked()) {
              categories.setMedicalDeviceSafety(true);
            } else {
              categories.setMedicalDeviceSafety(false);
            }
            if (mCategory7SelectBtn.isChecked()) {
              categories.setObservationalStudies(true);
            } else {
              categories.setObservationalStudies(false);
            }
            if (mCategory8SelectBtn.isChecked()) {
              categories.setPublicHealth(true);
            } else {
              categories.setPublicHealth(false);
            }
            if (mCategory9SelectBtn.isChecked()) {
              categories.setRadiationEmittingProducts(true);
            } else {
              categories.setRadiationEmittingProducts(false);
            }
            if (mCategory10SelectBtn.isChecked()) {
              categories.setTobaccoUse(true);
            } else {
              categories.setTobaccoUse(false);
            }

            filter.setStudyStatus(studyStatus);
            filter.setParticipationStatus(participationStatus);
            filter.setCategories(categories);

            boolean flag1;
            boolean flag2;
            boolean flag3;
            // enable/disable Apply button
            if (!filter.getStudyStatus().isActive()
                && !filter.getStudyStatus().isPaused()
                && !filter.getStudyStatus().isUpcoming()
                && !filter.getStudyStatus().isClosed()) {
              flag1 = true;
            } else {
              flag1 = false;
            }

            if (!filter.getCategories().isBiologicsSafety()
                && !filter.getCategories().isClinicalTrials()
                && !filter.getCategories().isCosmeticsSafety()
                && !filter.getCategories().isDrugSafety()
                && !filter.getCategories().isMedicalDeviceSafety()
                && !filter.getCategories().isObservationalStudies()
                && !filter.getCategories().isPublicHealth()
                && !filter.getCategories().isRadiationEmittingProducts()
                && !filter.getCategories().isTobaccoUse()) {
              flag3 = true;
            } else {
              flag3 = false;
            }

            if (mUserId.equalsIgnoreCase("")) {
              if (flag1 || flag3) {
                Toast.makeText(
                        FilterActivity.this,
                        getResources().getString(R.string.search_data_empty),
                        Toast.LENGTH_LONG)
                    .show();
              } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(filter); // obj is your object

                try {
                  JSONObject jsonObj = new JSONObject(json);
                  AppController.getHelperSharedPreference()
                      .writePreference(
                          FilterActivity.this,
                          getString(R.string.json_object_filter),
                          jsonObj.toString());
                  finish();
                } catch (JSONException e) {
                  Logger.log(e);
                }
              }
            } else {
              if (!filter.getParticipationStatus().isInProgress()
                  && !filter.getParticipationStatus().isYetToJoin()
                  && !filter.getParticipationStatus().isCompleted()
                  && !filter.getParticipationStatus().isWithdrawn()
                  && !filter.getParticipationStatus().isNotEligible()) {
                flag2 = true;
              } else {
                flag2 = false;
              }

              if (flag1 || flag2 || flag3) {
                Toast.makeText(
                        FilterActivity.this,
                        getResources().getString(R.string.search_data_empty),
                        Toast.LENGTH_LONG)
                    .show();
              } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(filter); // obj is your object

                try {
                  JSONObject jsonObj = new JSONObject(json);
                  AppController.getHelperSharedPreference()
                      .writePreference(
                          FilterActivity.this,
                          getString(R.string.json_object_filter),
                          jsonObj.toString());
                  finish();
                } catch (JSONException e) {
                  Logger.log(e);
                }
              }
            }
          }
        });
  }

  private void setFilterSelected(String jsonObjectString) {

    try {
      JSONObject jsonObj = new JSONObject(jsonObjectString);
      JSONObject studyStatus = jsonObj.getJSONObject("studyStatus");
      JSONObject participationStatus = jsonObj.getJSONObject("participationStatus");
      JSONObject categories = jsonObj.getJSONObject("categories");
      if (jsonObj.getBoolean("bookmarked")) {
        mBookmarkedSelctBtn.setChecked(true);
      }
      if (studyStatus.getBoolean("active")) {
        mActiveSelectBtn.setChecked(true);
      } else {
        mActiveSelectBtn.setChecked(false);
      }
      if (studyStatus.getBoolean("paused")) {
        mPausedSelectBtn.setChecked(true);
      } else {
        mPausedSelectBtn.setChecked(false);
      }
      if (studyStatus.getBoolean("upcoming")) {
        mUpcomingSelectBtn.setChecked(true);
      } else {
        mUpcomingSelectBtn.setChecked(false);
      }
      if (studyStatus.getBoolean("closed")) {
        mClosedSelectBtn.setChecked(true);
      } else {
        mClosedSelectBtn.setChecked(false);
      }

      if (participationStatus.getBoolean("inProgress")) {
        mInProgressSelctBtn.setChecked(true);
      } else {
        mInProgressSelctBtn.setChecked(false);
      }
      if (participationStatus.getBoolean("yetToJoin")) {
        mYettoJoinSelctBtn.setChecked(true);
      } else {
        mYettoJoinSelctBtn.setChecked(false);
      }
      if (participationStatus.getBoolean("completed")) {
        mCompletedSelectBtn.setChecked(true);
      } else {
        mCompletedSelectBtn.setChecked(false);
      }
      if (participationStatus.getBoolean("withdrawn")) {
        mWithdrawnSelectBtn.setChecked(true);
      } else {
        mWithdrawnSelectBtn.setChecked(false);
      }
      if (participationStatus.getBoolean("notEligible")) {
        mNotEligibleSelectBtn.setChecked(true);
      } else {
        mNotEligibleSelectBtn.setChecked(false);
      }

      if (categories.getBoolean("biologicsSafety")) {
        mCategory1SelectBtn.setChecked(true);
      } else {
        mCategory1SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("clinicalTrials")) {
        mCategory2SelectBtn.setChecked(true);
      } else {
        mCategory2SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("cosmeticsSafety")) {
        mCategory3SelectBtn.setChecked(true);
      } else {
        mCategory3SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("drugSafety")) {
        mCategory4SelectBtn.setChecked(true);
      } else {
        mCategory4SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("foodSafety")) {
        mCategory5SelectBtn.setChecked(true);
      } else {
        mCategory5SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("medicalDeviceSafety")) {
        mCategory6SelectBtn.setChecked(true);
      } else {
        mCategory6SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("observationalStudies")) {
        mCategory7SelectBtn.setChecked(true);
      } else {
        mCategory7SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("publicHealth")) {
        mCategory8SelectBtn.setChecked(true);
      } else {
        mCategory8SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("radiationEmittingProducts")) {
        mCategory9SelectBtn.setChecked(true);
      } else {
        mCategory9SelectBtn.setChecked(false);
      }
      if (categories.getBoolean("tobaccoUse")) {
        mCategory10SelectBtn.setChecked(true);
      } else {
        mCategory10SelectBtn.setChecked(false);
      }

    } catch (JSONException e) {
      Logger.log(e);
    }
  }
}
