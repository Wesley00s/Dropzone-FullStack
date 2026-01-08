import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FileManagerFeature } from './file-manager.feature';

describe('FileManagerFeature', () => {
  let component: FileManagerFeature;
  let fixture: ComponentFixture<FileManagerFeature>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileManagerFeature]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FileManagerFeature);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
