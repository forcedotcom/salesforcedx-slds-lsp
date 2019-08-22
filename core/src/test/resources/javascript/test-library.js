function funBuilder() {

    var createParty = function() {
        var attributes = {
            label: $A.get('$Label.Party.Create')
        };

        return ['ui:button', attributes];
    };

    var createHappyHour = function() {
        var attributes = {
            label: $A.get('$Label.HappyHour.Create')
        };

        return ['ui:button', attributes];
    };

    var createGetTogether = function() {
        var attributes = {
            label: $A.get('$Label.GetTogether.Create')
        };

        return ['ui:button', attributes];
    };

    var decorateCreateParty = function(createPartyButton) {
        if (createPartyButton.isValid()) {
            createPartyButton.set('v.class', 'slds-button slds-button--brand');
        }
    };

    var decorateCreateHappyHour = function(createHappyHourButton) {
        if (createHappyHourButton.isValid()) {
            createHappyHourButton.set('v.class', 'slds-button slds-button--neutral');
        }
    };

    var decorateCreateGetTogether = function(createGetTogetherButton) {
        if (createGetTogetherButton.isValid()) {
            createGetTogetherButton.set('v.class', 'slds-float--left');
        }
    };

    return {

        createParty: createParty,


        createHappyHour: createHappyHour,


        createGetTogether: createGetTogether,


        decorateCreateParty: decorateCreateParty,


        decorateCreateHappyHour: decorateCreateHappyHour,


        decorateCreateGetTogether: decorateCreateGetTogether
    };
}