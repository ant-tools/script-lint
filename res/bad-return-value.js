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
};

comp.prj.Class.prototype =
{
    m1: function() {
        return true;
    },

    /**
     * @return Number
     */
    m2: function() {
        return 'string';
    },

    /**
     * @return Number
     */
    m3: function() {
        return true;
    },

    /**
     * @return Number
     */
    m4: function() {
        return new js.net.XHR();
    },

    /**
     * @return String
     */
    m5: function() {
        return 123;
    },

    /**
     * @return String
     */
    m6: function() {
        return true;
    },

    /**
     * @return String
     */
    m7: function() {
        return new js.net.XHR();
    },

    /**
     * @return Boolean
     */
    m8: function() {
        return new js.net.XHR();
    }
};
$extends(comp.prj.Class, js.lang.Object);
