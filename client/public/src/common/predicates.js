define(() => ({
    is(value) {
        return (value2) => value2 === value;
    },
    isSet() {
        return (value) => !!value;
    }
}));
