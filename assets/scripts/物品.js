function newItem(name) {
	exports[name] = (() => {
		let myItem = extend(Item, name, {});
		return myItem;
	})();
}
newItem("phantom-steel")
newItem("phantom-titanium-steel")
newItem("mystic-crystal")