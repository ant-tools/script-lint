$strict();
$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    var v =
    {
        member: 1,
        member: 2
    }
};

comp.prj.Class.prototype =
{
    /**
     * Field.
     * @type Number
     */
    member: 123,

    /**
     * Method.
     * @return Object
     */
    member: function() {
        return {
            member: 1,
            member: 2
        }
    }
};
$extends(comp.prj.Class, js.lang.Object);

/**
 * Utility.
 */
comp.prj.Utility =
{
    /**
     * Field.
     * @type Number
     */
    member: 123,


    /**
     * Method.
     */
    member: function() {
    }
};

/**
 * Enumeration.
 */
comp.prj.Enumeration =
{
    /**
     * @type Number
     */
    CONSTANT: 1,

    /**
     * @type Number
     */
    CONSTANT: 2
};
