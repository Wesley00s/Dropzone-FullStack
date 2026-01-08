import { Routes } from '@angular/router';
import {FileManagerFeature} from './features/file-manager/file-manager.feature';

export const routes: Routes = [
  { path: '', component: FileManagerFeature },
];
