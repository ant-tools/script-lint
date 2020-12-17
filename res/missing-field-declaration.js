$strict();
$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    /**
     * Field.
     * @type Number
     */
    this._field1 = null;
};

/**
 * Static field.
 * @type Number
 */
comp.prj.Class.staticField = null;

/**
 * Static method.
 */
comp.prj.Class.staticMethod = function() {

};

$static(comp.prj.Class,
{
    staticField: null,
    staticMethod: function() {

    }
});

comp.prj.Class.prototype =
{
    /**
     * Method.
     */
    method: function() {
		comp.prj.Class.staticField = null;
        this._field1 = null;
        this._field2 = null;
    }
};
$extends(comp.prj.Class, js.lang.Object);
