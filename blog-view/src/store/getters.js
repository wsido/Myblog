export default {
	isUserLoggedIn: state => !!state.userToken && state.user && Object.keys(state.user).length > 0,
	loggedInUser: state => state.user,
	userToken: state => state.userToken,
}