/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.harvard.usermodule.event;

import com.harvard.webservicemodule.events.ResponseServerConfigEvent;

public class ActivityStateEvent {
  private ResponseServerConfigEvent responseServerConfigEvent;

  public ResponseServerConfigEvent getResponseServerConfigEvent() {
    return responseServerConfigEvent;
  }

  public void setResponseServerConfigEvent(ResponseServerConfigEvent responseServerConfigEvent) {
    this.responseServerConfigEvent = responseServerConfigEvent;
  }
}
