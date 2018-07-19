import * as React from 'react';
import { connect } from 'react-redux';

import { withBreadcrumb } from '../../../../../../components/BreadcrumbWrapper/BreadcrumbWrapper';
import { UserProfile } from '../../../../../../modules/api/jophiel/userProfile';
import { ProfilePanel } from '../../../../panels/profile/Profile/Profile';
import { AppState } from '../../../../../../modules/store';
import { selectProfile } from '../../../../../../modules/session/sessionSelectors';
import { profileActions as injectedProfileActions } from '../modules/profileActions';

interface ProfilePageProps {
  profile?: UserProfile;
  onGetProfile: () => void;
  onUpdateProfile: (profile: UserProfile) => Promise<void>;
}

class ProfilePage extends React.PureComponent<ProfilePageProps> {
  componentDidMount() {
    this.props.onGetProfile();
  }

  render() {
    if (!this.props.profile) {
      return null;
    }
    return <ProfilePanel profile={this.props.profile} onUpdateProfile={this.props.onUpdateProfile} />;
  }
}

export function createProfilePage(profileActions) {
  const mapStateToProps = (state: AppState) =>
    ({
      profile: selectProfile(state),
    } as Partial<ProfilePageProps>);
  const mapDispatchToProps = {
    onGetProfile: profileActions.getProfile,
    onUpdateProfile: profileActions.updateProfile,
  };
  return connect(mapStateToProps, mapDispatchToProps)(ProfilePage);
}

export default withBreadcrumb('Profile')(createProfilePage(injectedProfileActions));