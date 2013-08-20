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
        self.perPage = 15;
        self.resetList();
    },

    refreshList: function() {
    	var self = this;

    	$('#tblTables tr:gt(1)').remove();
    	$('#tblTables tr:eq(1)').show();

    	var tables = [];
    	var start = (self.currentPage - 1) * self.perPage;
    	for (var i = 0; i < self.perPage; ++i) {
    		if (start >= self.currentList.length) {
    			break;
    		}
    		tables.push(self.currentList[start + i]);
    	}

    	if (tables.length == 0) {
    		$('#tblTables tr:eq(1)').hide();
    		$('<tr><td colspan="6" align="center">No Result</td></tr>').appendTo('#tblTables');
    		return;
    	}

    	var data = {
    		databaseId: self.databaseId,
    		tables: tables.join(',')
    	};

    	$.getJSON('/table/get_info/', data, function(response) {
    		$('#tblTables tr:eq(1)').hide();
    		$.each(tables, function(i) {
    			if (!(this in response)) {
    				return true;
    			}
    			var tableViewParams = $.param({
    				databaseId: self.databaseId,
    				table: this
    			});
    			var tr = '<tr>'
    				   + '<td>' + (start + i + 1) + '</td>'
    				   + '<td>' + this + '</td>'
    				   + '<td>' + response[this].columnCount + '</td>'
    				   + '<td>' + response[this].rowCount + '</td>'
    				   + '<td>' + response[this].dataLength + '</td>'
    				   + '<td><a href="/column/list/?' + tableViewParams + '">Detail</a></td>'
    				   + '</tr>';
    			$(tr).appendTo('#tblTables');
    		});
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
