var TableList = function(opts) {
	var self = this;
	self.databaseId = opts.databaseId;
	self.tableNameList = opts.tableNameList;

	self.initList();
	self.initSearch();
	self.initPagination();
};

TableList.prototype = {
    constructor: TableList,

    initList: function() {
        var self = this;
        self.perPage = 32;
        self.resetList();
    },

    refreshList: function() {
    	var self = this;

    	$('#ulTables li').remove();

    	var tables = [];
    	var start = (self.currentPage - 1) * self.perPage;
    	for (var i = 0; i < self.perPage; ++i) {
    		if (start >= self.currentList.length) {
    			break;
    		}
    		tables.push(self.currentList[start + i]);
    	}

    	if (tables.length == 0) {
    		$('<li><strong>No result.</strong></li>').appendTo('#ulTables');
    		return;
    	}

    	$.each(tables, function(i, tableName) {
    		var li = '<li><a href="javascript:void(0);" title="' + tableName + '">'
    			   + '<i class="icon-list-alt"></i> ' + tableName
    			   + '</a></li>';
    		var $li = $(li);
    		$li.appendTo('#ulTables');
    	});

    },

    resetList: function() {
    	var self = this;
    	self.currentList = self.tableNameList;
    	self.currentPage = 1;
    	self.totalPage = Math.ceil(self.currentList.length / self.perPage);
    	self.refreshList();
    },

    initSearch: function() {
    	var self = this;

    	var timeoutHandler = null;
    	$('#txtSearch').keyup(function() {
    		if (timeoutHandler != null) {
    			clearTimeout(timeoutHandler);
    		}
    		timeoutHandler = setTimeout(function() {
    			self.search();
    		}, 500);
    	});
    },

    search: function() {
    	var self = this;

    	var keyword = $.trim($('#txtSearch').val());
    	if (!keyword) {
    		self.resetList();
    		return;
    	}

    	keyword = keyword.toLowerCase();
    	self.currentList = [];
    	$.each(self.tableNameList, function() {
    		if (this.toLowerCase().indexOf(keyword) != -1) {
    			self.currentList.push(this);
    		}
    	});

    	self.currentPage = 1;
    	self.totalPage = Math.ceil(self.currentList.length / self.perPage);
    	self.refreshList();
    },

    initPagination: function() {
    	var self = this;

    	$('#btnPage').click(function() {
    		var page = parseInt($('#txtPage').val());
    		if (!page) {
    			return false;
    		}
    		if (page < 1 || page > self.totalPage) {
    			return false;
    		}
    		self.currentPage = page;
    		self.refreshList();
    	});

    },

    _theEnd: undefined
};
