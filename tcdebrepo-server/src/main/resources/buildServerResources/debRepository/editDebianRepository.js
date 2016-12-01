DebRepoPlugin = {
    removeDebRepo: function(uuid) {
    	DebRepoPlugin.DeleteRepoDialog.showDialog("Delete Debian Repository", 'deleteDebRepo', uuid);
    },
    DeleteRepoDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('deleteRepoDialog');
    	},
    	
    	formElement: function () {
    		return $('deleteRepoForm');
    	},
    	
    	showDialog: function (title, action, uuid) {
    		$j("#deleteRepoForm input[id='DebRepoaction']").val(action);
    		$j("#deleteRepoDialog .dialogTitle").html(title);
    		this.cleanFields(uuid);
    		this.cleanErrors();
    		this.showCentered();
    	},
    	
    	cleanFields: function (uuid) {
    		$j("#deleteRepoForm input[id='debrepo.uuid']").val(uuid);
    		
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#deleteRepoForm .error").remove();
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
    		var next = $j("#ajaxRepoDeleteResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxRepoDeleteResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doValidate: function() {
    		var errorFound = false;
    		return !errorFound;
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		if (!this.doValidate()) {
    			return false;
    		}
    		
    		var parameters = {
    				action: $j("#deleteRepoForm #DebRepoaction").val(),
    				"debrepo.uuid": $j("#deleteRepoForm input[id='debrepo.uuid']").val()
    		};
    		
    		var dialog = this;
    		
    		BS.ajaxRequest(window['base_uri'] + '/admin/debianRepositoryAction.html', {
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
    					window.location = window['base_uri'] + '/admin/debianRepositories.html'
    					$("repoBuildTypesContainer").refresh();
    					dialog.close();
    				}
    			}
    		});
    		
    		return false;
    	}
    }))
};


DebRepoFilterPlugin = {
    editFilter: function(data) {
    	DebRepoFilterPlugin.RepoEditFilterDialog.showDialog("Edit Artfact Filter", 'editArtifactFilter', data);
        //$j(".runnerFormTable input[id='filter.id']").prop("disabled", true);
    },
    addFilter: function(data) {
    	DebRepoFilterPlugin.RepoEditFilterDialog.showDialog("Add Artifact Filter", 'addArtifactFilter', data);
    },
    deleteFilter: function(data) {
    	DebRepoFilterPlugin.RepoDeleteFilterDialog.showDialog("Delete Artifact Filter", 'deleteArtifactFilter', data);
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
            $j(".dialogTitle").html(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("#repoEditFilterForm input[id='debrepo.uuid']").val(data.uuid);
            $j("#repoEditFilterForm input[id='debrepofilter.id']").val(data.id);
            $j(".runnerFormTable input[id='debrepofilter.regex']").val(data.regex);
            $j(".runnerFormTable input[id='debrepofilter.dist']").val(data.dist);
            $j(".runnerFormTable input[id='debrepofilter.component']").val(data.component);
            $j(".runnerFormTable select[id='debrepofilter.buildtypeid']").val(data.build);
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
            var thing = $j(".runnerFormTable input[id='debrepofilter.regex']").val();
            console.log(thing);
            thing = JSON.stringify($j(".runnerFormTable input[id='debrepofilter.regex']").val()).slice(1, -1);
            console.log(thing);

            var parameters = {
                action: $j("#repoEditFilterForm #DebRepoaction").val(),
                "debrepo.filter.id": $j("#repoEditFilterForm input[id='debrepofilter.id']").val(),
                "debrepo.filter.regex": $j(".runnerFormTable input[id='debrepofilter.regex']").val(),
                "debrepo.filter.dist": $j(".runnerFormTable input[id='debrepofilter.dist']").val(),
                "debrepo.filter.component": $j(".runnerFormTable input[id='debrepofilter.component']").val(),
            	"debrepo.filter.buildtypeid": $j(".runnerFormTable select[id='debrepofilter.buildtypeid']").val(),
                "debrepo.uuid": $j("#repoEditFilterForm input[id='debrepo.uuid']").val()
            };

             var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/debianRepositoryAction.html', {
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
                        $("repoBuildTypesContainer").refresh();
                        dialog.close();
                    }
                }
            });

            return false;
        }
    })),
    
    RepoDeleteFilterDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('repoDeleteFilterDialog');
    	},
    	
    	formElement: function () {
    		return $('repoDeleteFilterForm');
    	},
    	
    	showDialog: function (title, action, data) {
    		$j("input[id='DebRepoaction']").val(action);
    		$j(".dialogTitle").html(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
    	},
    	
    	cleanFields: function (data) {
    		$j("#repoDeleteFilterForm input[id='debrepo.uuid']").val(data.uuid);
    		$j("#repoDeleteFilterForm input[id='debrepofilter.id']").val(data.id);
    		$j("#repoDeleteFilterForm input[id='debrepofilter.buildtypeid']").val(data.build);
    		
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#repoDeleteFilterForm .error").remove();
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
    		var next = $j("#ajaxDeleteResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxDeleteResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doValidate: function() {
    		var errorFound = false;
    		return !errorFound;
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		if (!this.doValidate()) {
    			return false;
    		}
    		
    		var parameters = {
    				action: $j("#repoDeleteFilterForm #DebRepoaction").val(),
    				"debrepo.filter.id": $j("#repoDeleteFilterForm input[id='debrepofilter.id']").val(),
    				"debrepo.filter.buildtypeid": $j("#repoDeleteFilterForm input[id='debrepofilter.buildtypeid']").val(),
    				"debrepo.uuid": $j("#repoDeleteFilterForm input[id='debrepo.uuid']").val()
    		};
    		
    		var dialog = this;
    		
    		BS.ajaxRequest(window['base_uri'] + '/admin/debianRepositoryAction.html', {
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
    					$("repoBuildTypesContainer").refresh();
    					dialog.close();
    				}
    			}
    		});
    		
    		return false;
    	}
    }))
};
