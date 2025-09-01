import { ProfileDetails } from './components/ProfileDetails';
import { PasswordDetails } from './components/PasswordDetails';

export const Profile: React.FC = () => (
  <div className="grid grid-cols-1 grid-rows-2 lg:grid-cols-2 lg:grid-rows-1 gap-4 p-4 lg:p-0">
    <ProfileDetails />
    <PasswordDetails />
  </div>
);
