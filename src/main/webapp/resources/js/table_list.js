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
        self.currentPage = 1;
        self.perPage = 15;
        self.totalPage = Math.ceil(self.tableNameList.length / self.perPage);
        self.refreshList();
    },

    refreshList: function() {
    	var self = this;

    	$('#tblTables tr:gt(1)').remove();
    	$('#tblTables tr:eq(1)').show();

    	var tables = [];
    	var start = (self.currentPage - 1) * self.perPage;
    	for (var i = 0; i < self.perPage; ++i) {
    		if (start >= self.tableNameList.length) {
    			break;
    		}
    		tables.push(self.tableNameList[start + i]);
    	}

    	var data = {
    		database_id: self.databaseId,
    		tables: tables.join(',')
    	};

    	$.getJSON('/table/get_info', data, function(response) {
    		$('#tblTables tr:eq(1)').hide();
    		$.each(tables, function(i) {
    			if (!(this in response)) {
    				return true;
    			}
    			var tr = '<tr>'
    				   + '<td>' + (start + i + 1) + '</td>'
    				   + '<td>' + this + '</td>'
    				   + '<td>' + response[this].columnCount + '</td>'
    				   + '<td>' + response[this].rowCount + '</td>'
    				   + '<td>' + response[this].dataLength + '</td>'
    				   + '<td>-</td>'
    				   + '</tr>';
    			$(tr).appendTo('#tblTables');
    		});
    	});
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

    	var keyword = $('#txtSearch').val();
    	console.log(keyword);
    },

    initPagination: function() {
    	var self = this;

    	$('#btnGo').click(function() {
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
