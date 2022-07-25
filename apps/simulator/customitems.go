package main

type CustomItem struct {
	Id               string `json:"id"`
	Name             string `json:"name"`
	Description      string `json:"description"`
	RequestTimestamp string `json:"requestTimestamp"`
}

func createCustomItems() []CustomItem {

	customItems := []CustomItem{}

	// Add valid items
	for i := 0; i < 12; i++ {
		customItems = append(customItems,
			CustomItem{
				Name:             "Name",
				Description:      "Description",
				RequestTimestamp: "RequestTimestamp",
			})
	}

	// Add invalid item - name
	customItems = append(customItems,
		CustomItem{
			Name:             "",
			Description:      "Description",
			RequestTimestamp: "RequestTimestamp",
		})

	// Add invalid item - description
	customItems = append(customItems,
		CustomItem{
			Name:             "Name",
			Description:      "",
			RequestTimestamp: "RequestTimestamp",
		})

	// Add invalid item - request timestamp
	customItems = append(customItems,
		CustomItem{
			Name:             "Name",
			Description:      "Description",
			RequestTimestamp: "",
		})

	return customItems
}
