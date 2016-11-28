
DebRepoFilterPlugin = {
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
    editFilter: function(data) {
    	DebRepoFilterPlugin.RepoEditFilterDialog.showDialog("Edit Debian Repository", 'editDebRepo', data);
        $j(".runnerFormTable input[id='filter.id']").prop("disabled", true);
    },
    addDebRepo: function(projectId) {
    	DebRepoFilterPlugin.RepoEditFilterDialog.showDialog("Add Debian Repository", 'addDebRepo', {uuid: '', name: '', projectId: projectId});
    },
    RepoEditFilterDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('repoEditFilterDialog');
        },

        formElement: function () {
            return $('repoEditFilterForm');
        },

        showDialog: function (title, action, data) {
            $j("input[id='DebRepoaction']").val(action);
            $j(".dialogTitle").val(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("repoEditFilterForm input[id='debrepo.uuid']").val(data.uuid);
            $j(".runnerFormTable input[id='debrepofilter.regex']").val(data.regex);
            $j(".runnerFormTable input[id='debrepofilter.dist']").val(data.dist);
            $j(".runnerFormTable input[id='debrepofilter.component']").val(data.component);
            $j("#repoEditFilterForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#repoEditFilterForm .error").remove();
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
                action: $j("#repoEditFilterForm #DebRepoaction").val(),
                "debrepo.filter.id": $j(".runnerFormTable input[id='filter.id']").val(),
                "debrepo.uuid": $j("#repoEditFilterForm input[id='debrepo.uuid']").val()
            };

             var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/manageDebianRepository.html', {
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
