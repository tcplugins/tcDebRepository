/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

BS.DebRepoActions = {
  refreshDebRepoList: function() {
    $('debRepoList').refresh();
  }
};


BS.CreateDebRepoForm = OO.extend(BS.AbstractWebForm, {
  formElement: function() {
    return $('createDebRepo');
  },

  reset: function() {
    Form.reset(this.formElement());
    this.clearErrors();
  },

  focusFirstElement: function() {
    Form.focusFirstElement(this.formElement());
  },

  focusElement: function(elementId) {
    $(elementId).activate();
  },

  savingIndicator: function() {
    return $('createDebRepoProgress');
  },

  submit: function() {
    var that = this;
    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onCreateDebRepoError: function(elem) {
        $("error_createError").innerHTML = elem.firstChild.nodeValue;
      },

      onDebRepoNameError: function(elem) {
        $("error_debRepoName").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoName"));
      },

      onDebRepoDescriptionError: function(elem) {
        $("error_debRepoDescription").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoDescription"));
      },

      onDebRepoPriorityError: function(elem) {
        $("error_debRepoPriority").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoPriority"));
      },

      onCompleteSave: function(form, responseXML, err) {
        form.setSaving(false);
        if (err) {
          form.enable();
          form.focusFirstErrorField();
        } else {
          document.location = $("afterCreateLocation").value + "?debRepoId=" +
                              responseXML.documentElement.getElementsByTagName("debRepo")[0].getAttribute("id");
        }
      }
    }));
    return false;
  }
});

BS.EditDebRepoForm = OO.extend(BS.AbstractWebForm, {
  formElement: function() {
    return $('editDebRepo');
  },

  reset: function() {
    Form.reset(this.formElement());
    this.clearErrors();
  },

  focusFirstElement: function() {
    Form.focusFirstElement(this.formElement());
  },

  focusElement: function(elementId) {
    $(elementId).activate();
  },

  savingIndicator: function() {
    return $('editDebRepoProgress');
  },

  submit: function() {
    var that = this;
    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onDebRepoNotFound: function() {
        BS.reload(true);
      },

      onDebRepoNameError: function(elem) {
        $("error_debRepoName").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoName"));
      },

      onDebRepoDescriptionError: function(elem) {
        $("error_debRepoDescription").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoDescription"));
      },

      onDebRepoPriorityError: function(elem) {
        $("error_debRepoPriority").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("debRepoPriority"));
      },

      onSuccessfulSave: function() {
        BS.reload(true);
      }
    }));
    return false;
  }
});



BS.DeleteDebRepoDialog = OO.extend(BS.AbstractModalDialog, {
  getContainer: function() {
    return $('deleteDebRepoDialog');
  },

  getRefreshableContainer: function() {
    return $('deleteDebRepoRefreshableContainer');
  },

  showDeleteDialog: function(debRepoId, afterFinish) {
    var that = BS.DeleteDebRepoDialog;    
    this.afterFinish = afterFinish;    
    this.getRefreshableContainer().refresh(null, "debRepoId=" + encodeURIComponent(debRepoId), function() {
      that.updateDialog();      
      that.showCentered();
    });
  }
});

BS.DeleteDebRepoForm = OO.extend(BS.AbstractWebForm, {
  formElement: function() {
    return $('deleteDebRepo');
  },

  submit: function() {
    var that = this;
    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onMoveConfigurationsError: function(elem) {
        $("error_moveConfigurations").innerHTML = elem.firstChild.nodeValue;
      },

      onSuccessfulSave: function() {
        that.enable();
        BS.DeleteDebRepoDialog.close();
        BS.DeleteDebRepoDialog.afterFinish();
      }
    }, true));
    return false;
  }  
});



BS.UnassignBuildTypesForm = OO.extend(BS.AbstractWebForm, {
  formElement: function() {
    return $('unassignBuildTypesForm');
  },

  selectAll: function(select) {
    if (select) {
      BS.Util.selectAll(this.formElement(), "unassign");
    } else {
      BS.Util.unselectAll(this.formElement(), "unassign");
    }
  },

  selected: function() {
    var checkboxes = Form.getInputs(this.formElement(), "checkbox", "unassign");
    for (var i=0; i<checkboxes.length; i++) {
      if (checkboxes[i].checked) {
        return true;
      }
    }

    return false;
  },

  setSaving: function(saving) {
    if (saving) {
      BS.Util.show('unassignInProgress');
    } else {
      BS.Util.hide('unassignInProgress');
    }
  },

  submit: function() {
    if (!this.selected()) {
      alert("Please select at least one build configuration.");
      return false;
    }

    if (!confirm("Are you sure you want to unassign selected configurations?")) return false;

    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onCompleteSave: function() {
        $('pClassBuildTypesContainer').refresh();
      }
    }));

    return false;
  }
});

BS.AttachConfigurationsToClassDialog = OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
  nonDefaultMovedCount: 0,

  getContainer: function() {
    return $('attachConfigurationsToClassDialog');
  },

  selectAll: function(select) {
    if (select) {
      BS.Util.selectAll(this.formElement(), "configurationId");
    } else {
      BS.Util.unselectAll(this.formElement(), "configurationId");
    }
  },

  selectConfiguration: function(checkbox, isDefaultDebRepo) {
    if (!isDefaultDebRepo) {
      if (checkbox.checked) {
        BS.AttachConfigurationsToClassDialog.nonDefaultMovedCount++;
      } else {
        BS.AttachConfigurationsToClassDialog.nonDefaultMovedCount--;        
      }
    }
  },

  showAttachDialog: function(pClassId) {
    var that = BS.AttachConfigurationsToClassDialog;
    this.pClassId = pClassId;
    $('attachConfigurationsToClassContainer').refresh(null, "pClassId=" + encodeURIComponent(pClassId) + "&openDialog=true", function() {
      that.showCentered();
      that.bindCtrlEnterHandler(that.submit.bind(that));
      that.focusFirstElement();
    });
  },

  resetFilter: function() {
    $j('#searchString').val('');
    this.findConfigurations();
    return false;
  },

  findConfigurations: function() {
    var that = BS.AttachConfigurationsToClassDialog;
    var findProgress = $('findProgress');
    var pClassId = this.pClassId;
    var form = this.formElement();
    var parameters = "pClassId=" + encodeURIComponent(pClassId) + "&searchString=" + encodeURIComponent(form.searchString.value) + "&searchStringSubmitted=true";

    findProgress.show();
    $('configurationListRefreshable').refresh(null, parameters, function() {
      findProgress.hide();
      that.recenterDialog();
      that.updateDialog();
      that.focusFirstElement();
    });
    return false;
  },

  _onSuccess: function() {
    this.nonDefaultMovedCount = 0;
    $('pClassBuildTypesContainer').refresh();
    this.enable();
    this.close();
  },

  formElement: function() {
    return $('attachConfigurationsToClass');
  },

  savingIndicator: function() {
    return $('attachProgress');
  },

  focusFirstElement: function() {
    Form.focusFirstElement(this.formElement());
  },

  submit: function() {
    this.formElement().submitAction.value='assignConfigurations';
    if (this.nonDefaultMovedCount != 0) {
      var msg = "You select " + this.nonDefaultMovedCount + " configuration(s) from non-default priority class, " +
                "are you sure you want to move them in current priority class?";
      if (!confirm(msg)) return false;
    }
    var that = this;
    BS.FormSaver.save(this, this.formElement().action, OO.extend(BS.ErrorsAwareListener, {
      onAttachToGroupsError: function(elem) {
        $("error_attachToClass_" + that.formElement().id).innerHTML = elem.firstChild.nodeValue;
      },

      onSuccessfulSave: function() {
        that._onSuccess();
      }
    }));
    return false;
  }
}));



BS.DebRepoConfigurationsPopup = {};
BS.DebRepoConfigurationsPopup = new BS.Popup("debRepoConfigurationsPopup", {
  url: window['base_uri'] + "/plugins/priority-queue/debRepoConfigurationsPopup.html",
  method: "get"
});

BS.DebRepoConfigurationsPopup.showPopup = function(nearestElement, debRepoId) {
  this.options.parameters = "debRepoId=" + encodeURIComponent(debRepoId);
  this.showPopupNearElement(nearestElement);
};