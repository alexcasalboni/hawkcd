'use strict';

angular
    .module('hawk')
    .factory('viewModel', ['$rootScope', 'agentService', 'toaster', function ($rootScope, agentService, toaster) {
        var viewModel = this;

        viewModel.isAgentFirstRun = true;

        viewModel.allAgents = [];

        viewModel.allPipelines = [];

        viewModel.allMaterials = [];

        viewModel.allPipelineDefinitions = [];

        viewModel.allPipelineGroups = [];
        
        viewModel.allStages = [];

        viewModel.allJobs = [];
            
        viewModel.allPipelineRuns = [];

        return viewModel;
    }]);