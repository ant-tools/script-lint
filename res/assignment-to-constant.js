$strict()
$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    /**
     * Constant.
     * @type Number
     */
    this._CONSTANT = 123;
};

/**
 * Static constant.
 * @type Number
 */
comp.prj.Class._CONSTANT = 123;

comp.prj.Class.prototype =
{
    /**
     * Method.
     */
    method: function() {
        // assignment to own instance constant
        this._CONSTANT = 1000;

        // assignment to own static constant
        comp.prj.Class._CONSTANT = 1000;

        // assignemnt to foreign instance constant
        var rmi = new js.net.RMI();
        rmi.DELAY = 100;

        // assignment to foreign static constant
        js.net.XHR.TIMEOUT = 4000;
    }
};
$extends(comp.prj.Class, js.lang.Object);
