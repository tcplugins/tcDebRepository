
DebRepoPlugin = {
    removeRepo: function(projectId, serverId) {
        if (!confirm("The repository will be permanently deleted. Proceed?")) {
            return;
        }
        BS.ajaxRequest(window['base_uri'] + '/admin/tcDebRepository/manageDebianRepositories.html', {
            parameters: Object.toQueryString({
                action: 'removeRepo',
                projectId: projectId,
                'serverinfo.uuid': repoUuid
            }),
            onComplete: function(transport) {
                $("DebRepos").refresh();
            }
        });
    },
    editServer: function(data) {
    	DebRepoPlugin.RepoConfigurationDialog.showDialog('editRepo', data);
        $j(".runnerFormTable input[id='serverinfo.uuid']").prop("disabled", true);
    },
    addRepo: function(projectId) {
    	DebRepoPlugin.RepoConfigurationDialog.showDialog('addRepo', {uuid: '', name: '', projectId: projectId});
    },
    RepoConfigurationDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('repoConfigDialog');
        },

        formElement: function () {
            return $('repoConfigForm');
        },

        showDialog: function (action, data) {
            $j("input[id='DebRepoaction']").val(action);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("input[id='serverinfo.uuid']").val(data.uuid);
            $j(".runnerFormTable input[id='serverinfo.name']").val(data.name);
            $j("#debRepoForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#debRepoForm .error").remove();
        },

        error: function($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },

        doValidate: function() {
            var errorFound = false;

            var name = $j('input[id="serverinfo.name"]');
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
                "serverinfo.name": $j(".runnerFormTable input[id='serverinfo.name']").val(),
                "projectId": $j("#repoConfigForm #projectId").val(),
                action: $j("#debRepoForm #DebRepoaction").val(),
                "serverinfo.uuid": $j("#debRepoForm input[id='serverinfo.uuid']").val()
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
                                alert(error);
                            }
                            if (responseTag.getAttribute("status") == "OK") {
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
