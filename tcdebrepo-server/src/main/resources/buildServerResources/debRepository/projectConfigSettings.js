
DebRepoPlugin = {
    removeDebRepo: function(projectId, repoUuid) {
        if (!confirm("The repository will be permanently deleted. Proceed?")) {
            return;
        }
        BS.ajaxRequest(window['base_uri'] + '/admin/tcDebRepository/manageDebianRepositories.html', {
            parameters: Object.toQueryString({
                action: 'removeDebRepo',
                projectId: projectId,
                'debrepo.uuid': repoUuid
            }),
            onComplete: function(transport) {
                $("DebRepos").refresh();
            }
        });
    },
    editDebRepo: function(data) {
    	DebRepoPlugin.RepoConfigurationDialog.showDialog("Edit Debian Repository", 'editDebRepo', data);
        $j(".runnerFormTable input[id='debrepo.uuid']").prop("disabled", true);
    },
    addDebRepo: function(projectId) {
    	DebRepoPlugin.RepoConfigurationDialog.showDialog("Add Debian Repository", 'addDebRepo', {uuid: '', name: '', projectId: projectId});
    },
    RepoConfigurationDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('repoConfigDialog');
        },

        formElement: function () {
            return $('repoConfigForm');
        },

        showDialog: function (title, action, data) {
            $j("input[id='DebRepoaction']").val(action);
            $j(".dialogTitle").val(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("input[id='debrepo.uuid']").val(data.uuid);
            $j(".runnerFormTable input[id='debrepo.name']").val(data.name);
            $j("#repoConfigForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#repoConfigForm .error").remove();
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
                "projectId": $j("#repoConfigForm #projectId").val(),
                action: $j("#repoConfigForm #DebRepoaction").val(),
                "debrepo.uuid": $j("#repoConfigForm input[id='debrepo.uuid']").val()
            };

             var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/tcDebRepository/manageDebianRepositories.html', {
                parameters: parameters,
                onComplete: function(transport) {
                    var shouldClose = true;
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
                            } else if (responseTag.firstChild == null) {
                                shouldClose = false;
                                alert("Error: empty response");
                            }
                        }
                    }
                    if (shouldClose) {
                        $("DebRepos").refresh();
                        dialog.close();
                    }
                }
            });

            return false;
        }
    }))
};
