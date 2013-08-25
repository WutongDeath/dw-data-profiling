var ServerList = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;
    self.initDialog();
};

ServerList.prototype = {
    constructor: ServerList,

    initDialog: function() {
        var self = this;

        $('#btnAdd').click(function() {
            var data = {}, error = false;
            $.each(['serverName', 'host', 'port', 'username', 'password'], function() {
                var obj = $(':text[name="' + this + '"]');
                data[this] = obj.val();
                if (!data[this]) {
                    alert('Field "' + this + '" cannot be empty.');
                    error = true;
                    obj.focus();
                    return false;
                }
            });
            if (error) {
                return false;
            }

            $.post(self.contextPath + '/server/add/', data, function(result) {
                if (result.status == 'ok') {
                    window.location.reload();
                } else {
                    alert(result.msg);
                }
            }, 'json');
        });
    },

    _theEnd: undefined
};
