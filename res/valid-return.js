$strict();
$suppress('NO_APIDOC');
$suppress('NO_SUPER');
$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    /**
     * @type Number
     */
    this.field = 123;
};

comp.prj.Class.prototype =
{
    /**
     * @return Object
     */
    m0: function() {
        return call();
    },

    m1: function() {
        return;
    },

    m2: function() {
        return undefined;
    },

    /**
     */
    m3: function() {
        return;
    },

    /**
     */
    m4: function() {
        return undefined;
    },

    /**
     * @return Object
     */
    m5: function() {
        return call();
    },

    /**
     * @return Number
     */
    m6: function() {
        return 123;
    },

    /**
     * @return Number
     */
    m7: function() {
        return undefined;
    },

    /**
     * @return Number
     */
    m8: function() {
        return null;
    },

    /**
     * @return Number
     */
    m9: function() {
        return call();
    },

    /**
     * @return String
     */
    m10: function() {
        return 'string';
    },

    /**
     * @return String
     */
    m11: function() {
        return undefined;
    },

    /**
     * @return String
     */
    m12: function() {
        return null;
    },

    /**
     * @return String
     */
    m13: function() {
        return call();
    },

    /**
     * @return Boolean
     */
    m14: function() {
        return true;
    },

    /**
     * @return Boolean
     */
    m15: function() {
        return 123;
    },

    /**
     * @return Boolean
     */
    m16: function() {
        return 'string';
    },

    /**
     * @return Boolean
     */
    m17: function() {
        return call();
    },

    /**
     * @return Function
     */
    m18: function() {
        return function() {
        };
    },

    /**
     * @return Function
     */
    m19: function() {
        return call();
    },

    /**
     * @return comp.prj.Class
     */
    m20: function() {
        return new comp.prj.Class();
    },

    /**
     * @return comp.prj.Class
     */
    m21: function() {
        return call();
    },

    /**
     * @return Object
     */
    m22: function() {
        return new comp.prj.Class();
    },

    /**
     * @return Date
     */
    m23: function() {
        return new Date();
    },

    /**
     * @return Object
     */
    m24: function() {
        return call();
    },

    /**
     * @return Number
     */
    m25: function() {
        return this.field;
    },

    /**
     * @return String
     */
    m26: function() {
        var v = 'string';
        return v;
    },

    /**
     * @return Number
     */
    m27: function() {
        var v = 123;
        return v;
    },

    /**
     * @return Boolean
     */
    m28: function() {
        var v = true;
        return v;
    },

    /**
     * @return Number
     */
    m29: function() {
        var v = this.field;
        return v;
    },

    /**
     * @return Date
     */
    m30: function() {
        var v = new Date();
        return v;
    },

    /**
     * @return comp.prj.Class
     */
    m31: function() {
        var v = new comp.prj.Class();
        return v;
    },

    /**
     * @return Object
     */
    m32: function() {
        var v = new XMLHttpRequest();
        return v;
    },

    /**
     * @return comp.prj.Class
     */
    m33: function() {
        return this;
    },

    /**
     * @return String
     */
    m34: function() {
        return this.field.name;
    },

    /**
     * @type js.net.XHR.Request
     */
    m35: function() {
        var xhr = new js.net.XHR();
        return xhr.request;
    },

    m36: function() {
        /**
         * @return Number
         */
        function fn() {
            return 123;
        }
    },

    /**
     * @return Array
     */
    m37: function() {
        return [];
    },

    /**
     * @return Array
     */
    m38: function() {
        var a = [];
        return a;
    },

    /**
     * @return Boolean
     */
    m39: function() {
        return 'string' && 123;
    },

    /**
     * @return Object
     */
    m40: function() {
        return h ? h : null;
    }
};
$extends(comp.prj.Class, js.lang.Object);

$legacy(js.ua.Engine.TRIDENT, function() {
    /**
     * @return String
     */
    comp.prj.Class.prototype.m0 = function() {
        return 'string';
    };

    /**
     * @return Number
     */
    comp.prj.Class.prototype.m1 = function() {
        return 123;
    };

    /**
     * @return Boolean
     */
    comp.prj.Class.prototype.m2 = function() {
        return true;
    };

    /**
     * @return comp.prj.Class
     */
    comp.prj.Class.prototype.m3 = function() {
        return this;
    };

    comp.prj.Class.prototype.m4 = function() {
        return undefined;
    };

    comp.prj.Class.prototype.m5 = function() {
        return undefined;
    };
});
