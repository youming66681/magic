function newItem(name) {
	exports[name] = (() => {
		let myItem = extend(Item, name, {});
		return myItem;
	})();
}
newItem("phantomSteel")
newItem("phantomTitaniumSteel")
newItem("mysticCrystal")