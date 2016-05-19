/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.flvv.sample.features.showcase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import im.ene.lab.flvv.sample.R;
import im.ene.lab.flvv.sample.model.Model;
import im.ene.lab.flvv.sample.model.VideoModel;
import java.util.List;

/**
 * Created by eneim on 5/19/16.
 */

public class ShowcaseFragment extends Fragment implements ShowcaseView {

  public static ShowcaseFragment newInstance() {
    return new ShowcaseFragment();
  }

  private ShowcasePresenter presenter;

  private TextView textView;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_showcases, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    textView = (TextView) view.findViewById(R.id.loading);

    if (presenter == null) {
      presenter = new ShowcasePresenterImpl();
    }
    presenter.onViewCreated(this);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    presenter.onViewDestroyed(this);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    presenter.loadItems(true, new ShowcasePresenter.DataCallback() {
      @Override public void onDataLoaded(List<VideoModel> items) {

      }
    });
  }

  @Override public void showLoading(boolean isLoading) {
    if (textView != null) {
      textView.setText("LOADING:" + Boolean.toString(isLoading));
    }
  }

  @Override public void updateData(boolean forceUpdate, List<? extends Model> items) {

  }
}
