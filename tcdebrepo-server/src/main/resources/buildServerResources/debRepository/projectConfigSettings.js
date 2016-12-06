
DebRepoPlugin = {
   addDebRepo: function(projectId) {
    	DebRepoPlugin.AddRepoDialog.showDialog("Add Debian Repository", 'addDebRepo', {uuid: '', name: '', projectId: projectId});
    },
    AddRepoDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('addRepoDialog');
        },

        formElement: function () {
            return $('addRepoForm');
        },

        showDialog: function (title, action, data) {
            $j("input[id='DebRepoaction']").val(action);
            $j(".dialogTitle").val(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j(".runnerFormTable input[id='debrepo.name']").val(data.name);
            $j("#addRepoForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#addRepoForm .error").remove();
        },

        error: function($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },
        
        ajaxError: function(message) {
        	var next = $j("#ajaxResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxResult").after("<p class='error'>" + message + "</p>");
        	}
        },

        doValidate: function() {
            var errorFound = false;

            var name = $j('input[id="debrepo.name"]');
            if (name.val() == "") {
                this.error(name, "Please set the repository name");
                errorFound = true;
            }

            return !errorFound;
        },

        doPost: function() {
            this.cleanErrors();

            if (!this.doValidate()) {
                return false;
            }

            var parameters = {
                "debrepo.name": $j(".runnerFormTable input[id='debrepo.name']").val(),
                "debrepo.project.id": $j("#addRepoForm #projectId").val(),
                action: $j("#addRepoForm #DebRepoaction").val()
            };

             var dialog = this;

     		 BS.ajaxRequest(window['base_uri'] + '/admin/debianRepositoryAction.html', {
    			parameters: parameters,
    			onComplete: function(transport) {
    				var shouldClose = true;
    				var shouldRedirect = false;
    				if (transport != null && transport.responseXML != null) {
    					var response = transport.responseXML.getElementsByTagName("response");
    					if (response != null && response.length > 0) {
    						var responseTag = response[0];
    						var error = responseTag.getAttribute("error");
    						if (error != null) {
    							shouldClose = false;
    							dialog.ajaxError(error);
    						} else if (responseTag.getAttribute("status") == "OK") {
    							shouldClose = true;
    							if (responseTag.getAttribute("redirect") == "true") {
    								shouldRedirect = true;
    							}
    						} else if (responseTag.firstChild == null) {
    							shouldClose = false;
    							alert("Error: empty response");
    						}
    					}
    				}
    				if (shouldRedirect) {
    					dialog.close();
    					window.location = window['base_uri'] + '/admin/editDebianRepository.html?repo=' + $j("#addRepoForm input[id='debrepo.name']").val()
    				} else if (shouldClose) {
    					dialog.close();
    					$("DebRepos").refresh();
    				}

    			}
    		});
            return false;
        }
    }))
};
