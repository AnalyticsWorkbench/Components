//GRAVIS3D.Views.Meta = [
//		{
//			view: GRAVIS3D.Views.DifferenceGraphView,
//			thumbnail: "",
//			name: "Difference Graph"
//		},
//		{
//			view: GRAVIS3D.Views.TimeSliceView,
//			thumbnail: "",
//			name: "Time Slices"
//		}
//		
//];

GRAVIS3D.Views.Defaults = {
	rendering : {
		pauseAfter : 30000
	},
	basicView : {
		controls : THREE.OrbitControls,
		timeSliceGapScale : 1,
		timeSlicePlane : {
			show : true,
			padding : 10,
			color : "#000033",
			opacity : 0.03,
			showBorder : true,
			borderColor : "#FFFFFF",
			borderOpacity : 0.5,
			borderSize : 1
		},
		timeSliceEdges : {

		}
	},
	differenceGraphView : {
	}
};
