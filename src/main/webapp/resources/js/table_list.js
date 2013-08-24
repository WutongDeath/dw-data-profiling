var TableList = function(opts) {
    var self = this;
    self.databaseId = opts.databaseId;
    self.tableNameList = opts.tableNameList;
    self.tplInfo = Handlebars.compile($('#tplInfo').html());

    self.initList();
    self.initSearch();
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
        var divPagi = $('#divTables .pagination');

        $('#ulTables li').remove();

        var tables = [];
        var start = (self.currentPage - 1) * self.perPage;
        for (var i = 0; i < self.perPage; ++i) {
            if (start + i >= self.currentList.length) {
                break;
            }
            tables.push(self.currentList[start + i]);
        }

        if (tables.length == 0) {
            $('<li><strong>No result.</strong></li>').appendTo('#ulTables');
            divPagi.hide();
            return;
        }

        $.each(tables, function(i, tableName) {
            var li = '<li>'
                   + '<a href="/table/list/' + self.databaseId + '?' + $.param({table: tableName}) + '" title="' + tableName + '">'
                   + '<i class="icon-list-alt"></i> ' + tableName
                   + '</a></li>';
            var $li = $(li);

            $li.find('a').click(function() {
                self.refreshInfo(tableName);
                return false;
            });

            $li.appendTo('#ulTables');
        });

        // pagination
        var pOffset = 0;
        var pStart = self.currentPage - 4;
        if (pStart < 1) {
            pOffset = 1 - pStart;
            pStart = 1;
        }
        var pEnd = self.currentPage + 5 + pOffset;
        if (pEnd > self.totalPage) {
            pEnd = self.totalPage;
        }

        divPagi.find('ul li').remove();
        divPagi.find('ul').append(
                '<li' + (self.currentPage == 1 ? ' class="disabled"' : '') + '>'
                + '<a href="javascript:void(0);" page="' + (self.currentPage - 1) + '">Prev</a>'
                + '</li>');
        for (var p = pStart; p <= pEnd; ++p) {
            divPagi.find('ul').append(
                    '<li' + (p == self.currentPage ? ' class="active"' : '') + '>'
                    + '<a href="javascript:void(0);" page="' + p + '">' + p + '</a>'
                    + '</li>');
        }
        divPagi.find('ul').append(
                '<li' + (self.currentPage == self.totalPage ? ' class="disabled"' : '') + '>'
                + '<a href="javascript:void(0);" page="' + (self.currentPage + 1) + '">Next</a>'
                + '</li>');

        divPagi.find('a').click(function() {
            var li = $(this).parent();
            if (li.is('.disabled') || li.is('.active')) {
                return false;
            }
            self.currentPage = parseInt($(this).attr('page'));
            self.refreshList();
        });

        divPagi.show();

    },

    resetList: function() {
        var self = this;
        self.currentList = self.tableNameList;
        self.currentPage = 1;
        self.totalPage = Math.ceil(self.currentList.length / self.perPage);
        self.refreshList();
    },

    refreshInfo: function(tableName) {
        var self = this;

        var data = {
            databaseId: self.databaseId,
            table: tableName
        };

        $.getJSON('/table/get_info/', data, function(tableInfo) {

            if ($.isEmptyObject(tableInfo)) {
                alert("Fail to fetch table information.");
                $('#divTables').show();
                $('#divInfo').hide();
                return;
            }

            $('#divInfo').html(self.tplInfo(tableInfo));

            $('#divInfo').find('a[back]').click(function() {
                $('#divTables').show();
                $('#divInfo').hide();
            });

            $('#divInfo').find('a[refresh]').click(function() {
                self.refreshInfo(tableName);
            });

            if (tableInfo.status == 1) { // processing
                $('#divInfo').find('#lblStatus').addClass('label-info').text('Processing');
                $('#divInfo').find('#btnProfiling').addClass('disabled');
            } else {

                if (tableInfo.status == 2) { // processed
                    $('#divInfo').find('#lblStatus').addClass('label-success').text('Processed');
                } else {
                    $('#divInfo').find('#lblStatus').text('Not Profiled');
                }

                $('#divInfo').find('#btnProfiling').click(function() {
                    $.post('/table/start_profiling/', data, function(result) {
                        if (result.status == 'ok') {
                            self.refreshInfo(tableName);
                        } else {
                            alert(result.msg);
                        }
                    }, 'json');
                });
            }

            $('#divTables').hide();
            $('#divInfo').show();

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

        $('#divTables').show();
        $('#divInfo').hide();

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

    _theEnd: undefined
};
