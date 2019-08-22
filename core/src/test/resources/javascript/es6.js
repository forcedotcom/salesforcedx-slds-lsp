import { LightningElement, track } from 'lwc';

export default class App extends LightningElement {
    @track
    state = {
        title: 'Welcome to Lightning Web Components',
    };
    get options() {
        return [
            {
                label: 'supports',
                value: 'supports',
            },
            {
                label: 'has ability to show',
                value: 'shows',
            },
        ];
    };

    get style() {
        return 'slds-p-top_none';
    }
}
